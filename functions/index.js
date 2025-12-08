const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();


exports.sendOrderStatusNotification = onDocumentUpdated(
    "orders/{orderId}",
    async (event) => {
      const before = event.data.before.data();
      const after = event.data.after.data();

      // Pošalji notifikaciju samo ako se status promenio
      if (before.status === after.status) return;

      // Reaguj samo na "on_the_way"
      if (after.status !== "on_the_way") return;

      // Pretpostavljamo da svaki korisnik ima FCM token u Firestore-u npr:
      // users/{userId}/fcmToken: "XXX"
      const userId = after.userId;
      if (!userId) return;

      const userDoc = await admin.firestore()
          .collection("users")
          .doc(userId)
          .get();

      const userData = userDoc.data();
      const token = userData ? userData.fcmToken : null;


      if (!token) {
        console.log("❌ No token for user:", userId);
        return;
      }

      const message = {
        notification: {
          title: "📦 Your order is on the way!",
          body: `Order ${event.params.orderId} is being delivered.`,
        },
        token: token,
      };

      try {
        await admin.messaging().send(message);
        console.log(`🚀 Notification sent to user ${userId}`);
      } catch (error) {
        console.error("❌ Error sending notification:", error);
      }
    },
);
