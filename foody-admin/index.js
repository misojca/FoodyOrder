/*import admin from "firebase-admin";
import inquirer from "inquirer";
import fs from "fs";

// Učitaj svoj service account fajl
const serviceAccount = JSON.parse(fs.readFileSync("serviceAccountKey.json"));

// Inicijalizacija Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Funkcija za slanje FCM notifikacije korisniku
async function sendNotification(userId, orderId, status) {
  const userDoc = await db.collection("users").doc(userId).get();
  console.log("-------USER DOC-----------",userDoc);
  const token = userDoc.data()?.fcmToken;

  if (!token) {
    console.log(`❌ No FCM token found for user ${userId}`);
    return;
  }

  const message = {
    notification: {
      title: "📦 Order status updated!",
      body: `Your order ${orderId} is now ${status.replace("_", " ")}.`
    },
    token: token
  };

  try {
    await admin.messaging().send(message);
    console.log(`🚀 Notification sent to user ${userId}`);
  } catch (error) {
    console.error("❌ Error sending notification:", error);
  }
}

// Funkcija za update statusa porudžbine
async function updateOrderStatus() {
  const { orderId } = await inquirer.prompt([
    { type: "input", name: "orderId", message: "Enter Order ID:" }
  ]);

  const { newStatus } = await inquirer.prompt([
    {
      type: "list",
      name: "newStatus",
      message: "Select new status:",
      choices: ["pending", "processing", "on_the_way", "delivered", "cancelled"]
    }
  ]);

  const orderRef = db.collection("orders").doc(orderId);
  const orderDoc = await orderRef.get();
  const userId = orderDoc.data()?.userId;

  if (!orderDoc.exists) {
    console.log("❌ Order not found!");
    return;
  }

  await orderRef.update({ status: newStatus });

  console.log(`✔ Order ${orderId} updated to ${newStatus}`);

  if (newStatus === "on_the_way" && userId) {
    await sendNotification(userId, orderId, newStatus);
  }

  // Ako je delivered → obriši order i cart korisnika
  if (newStatus === "delivered" && userId) {
   // await orderRef.delete();
    //console.log(`🗑️ Order ${orderId} deleted (delivered).`);

    const cartSnapshot = await db.collection("users").doc(userId).collection("cart").get();
    const batch = db.batch();

    cartSnapshot.forEach(doc => batch.delete(doc.ref));
    await batch.commit();

    console.log(`🧹 Cart cleared for user ${userId}`);
  }
  
}


// Funkcija za listanje svih porudžbina
async function listOrders() {
  const snapshot = await db.collection("orders").get();

  console.log("\n📦 LIST OF ORDERS:\n");

  snapshot.forEach(doc => {
    const data = doc.data();
    console.log(`🆔 ${doc.id} | Status: ${data.status} | Total: ${data.totalPrice} | User: ${data.userId}`);
   
  });
}

// Glavna funkcija koja pokreće konzolu
async function main() {
  console.log("🔥 Foody Admin Console 🔥\n");

  while (true) {
    await listOrders();
    await updateOrderStatus();
    await new Promise(res => setTimeout(res, 1000)); // pauza 1s
  }
}

main();
*/

import admin from "firebase-admin";
import inquirer from "inquirer";
import fs from "fs";

// Učitaj svoj service account fajl
const serviceAccount = JSON.parse(fs.readFileSync("serviceAccountKey.json"));

// Inicijalizacija Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Funkcija za slanje FCM notifikacije korisniku (neizmenjena)
async function sendNotification(userId, orderId, status) {
  const userDoc = await db.collection("users").doc(userId).get();
  // Ne ispisujemo više ceo user doc u konzolu za čišći log
  const token = userDoc.data()?.fcmToken;

  if (!token) {
    console.log(`❌ No FCM token found for user ${userId}`);
    return;
  }

  const message = {
    notification: {
      title: "📦 Order status updated!",
      body: `Your order ${orderId} is now ${status.replace("_", " ")}.`
    },
    token: token
  };

  try {
    await admin.messaging().send(message);
    console.log(`🚀 Notification sent to user ${userId}`);
  } catch (error) {
    console.error("❌ Error sending notification:", error);
  }
}

// Funkcija za update statusa porudžbine (neizmenjena)
async function updateOrderStatus() {
  const { orderId } = await inquirer.prompt([
    { type: "input", name: "orderId", message: "Enter Order ID:" }
  ]);

  const { newStatus } = await inquirer.prompt([
    {
      type: "list",
      name: "newStatus",
      message: "Select new status:",
      choices: ["pending", "processing", "on_the_way", "delivered", "cancelled"]
    }
  ]);

  const orderRef = db.collection("orders").doc(orderId);
  const orderDoc = await orderRef.get();
  const userId = orderDoc.data()?.userId;

  if (!orderDoc.exists) {
    console.log("❌ Order not found!");
    return;
  }

  await orderRef.update({ status: newStatus });

  console.log(`✔ Order ${orderId} updated to ${newStatus}`);

  if (newStatus === "on_the_way" && userId) {
    await sendNotification(userId, orderId, newStatus);
  }

  // Ako je delivered → obriši order i cart korisnika
  if (newStatus === "delivered" && userId) {
    const cartSnapshot = await db.collection("users").doc(userId).collection("cart").get();
    const batch = db.batch();

    cartSnapshot.forEach(doc => batch.delete(doc.ref));
    await batch.commit();

    console.log(`🧹 Cart cleared for user ${userId}`);
  }
  
}


// Funkcija za listanje svih porudžbina - ISPRAVLJENA DA DOHVATA I IME KORISNIKA
async function listOrders() {
  const snapshot = await db.collection("orders").get();
  
  console.log("\n📦 LIST OF ORDERS:\n");

  // Niz za čuvanje svih asinhronih operacija dohvatanja imena korisnika
  const orderPromises = [];
  
  snapshot.forEach(doc => {
    const data = doc.data();
    const userId = data.userId;
    
    // Kreiranje promise za dohvatanje podataka o korisniku
    const userPromise = db.collection("users").doc(userId).get()
      .then(userDoc => {
        let userNameOrEmail = userId; // Default na userId ako ime nije pronađeno
        const userData = userDoc.data();
        
        // Pokušavamo dohvatiti 'email', a ako ne postoji, koristimo 'userId'
        if (userData && userData.email) {
            userNameOrEmail = userData.email;
        } 
        
        return {
          id: doc.id,
          status: data.status,
          totalPrice: data.totalPrice,
          userName: userNameOrEmail
        };
      })
      .catch(error => {
        console.error(`Error fetching user data for ${userId}:`, error.message);
        return {
          id: doc.id,
          status: data.status,
          totalPrice: data.totalPrice,
          userName: `${userId} (Error)`
        };
      });

    orderPromises.push(userPromise);
  });
  
  // Čekamo da se svi podaci (porudžbine i imena) dohvate
  const ordersWithNames = await Promise.all(orderPromises);

  // Ispisivanje podataka u konzolu
  ordersWithNames.forEach(order => {
    console.log(`🆔 ${order.id} | Status: ${order.status} | Total: ${order.totalPrice} | User: ${order.userName}`);
  });
}

// Glavna funkcija koja pokreće konzolu (neizmenjena)
async function main() {
  console.log("🔥 Foody Admin Console 🔥\n");

  while (true) {
    await listOrders();
    await updateOrderStatus();
    await new Promise(res => setTimeout(res, 1000)); // pauza 1s
  }
}

main();