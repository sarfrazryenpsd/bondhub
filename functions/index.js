const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendChatNotification = onDocumentCreated('messages/{baseChatId}/messages/{messageId}', async (event) => {
    try {
        const snapshot = event.data;
        if (!snapshot) {
            console.log('No data associated with the event');
            return null;
        }

        const messageData = snapshot.data();
        const senderId = messageData.senderId;
        const receiverId = messageData.receiverId;
        const content = messageData.content;
        const baseChatId = event.params.baseChatId;

        // Get receiver's FCM token
        const receiverDoc = await admin.firestore().collection('users').doc(receiverId).get();
        if (!receiverDoc.exists) {
            console.log('Receiver document not found');
            return null;
        }

        const receiverData = receiverDoc.data();
        const fcmToken = receiverData.fcmToken;

        if (!fcmToken) {
            console.log('No FCM token found for receiver');
            return null;
        }

        // Get sender's profile
        const senderDoc = await admin.firestore().collection('users').doc(senderId).get();
        if (!senderDoc.exists) {
            console.log('Sender document not found');
            return null;
        }

        const senderData = senderDoc.data();
        const senderName = senderData.displayName || 'User';
        const senderImage = senderData.profilePictureThumbnailUrl || '';

        // Get chat document for unread count
        const chatId = `${baseChatId}_${receiverId}`;
        const chatDoc = await admin.firestore().collection('chats').doc(chatId).get();
        let unreadCount = 1;

        if (chatDoc.exists) {
            const chatData = chatDoc.data();
            unreadCount = (chatData.unreadMessageCount || 0) + 1;

            // Update unread count in the chat document
            await admin.firestore().collection('chats').doc(chatId).update({
                unreadMessageCount: unreadCount,
                lastMessage: content,
                lastMessageTime: admin.firestore.FieldValue.serverTimestamp()
            });
        }

        // Prepare notification payload
        const payload = {
            data: {
                title: senderName,  // Use sender name as title
                message: content,
                senderName: senderName,
                senderImage: senderImage,
                chatId: chatId,
                baseChatId: baseChatId,
                senderId: senderId,
                unreadCount: unreadCount.toString()
            }
        };

        // Send the notification
        const response = await admin.messaging().sendToDevice(fcmToken, payload);
        console.log('Notification sent successfully:', response);

        return null;
    } catch (error) {
        console.error('Error sending notification:', error);
        return null;
    }
});