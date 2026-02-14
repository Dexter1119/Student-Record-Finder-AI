const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * ADMIN → ADD FACULTY
 */
exports.addFaculty = functions.https.onCall(async (data, context) => {

  // 🔐 AUTH CHECK
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Login required"
    );
  }

  if (context.auth.token.role !== "ADMIN") {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only admin can add faculty"
    );
  }

  const { name, email, password, department } = data;

  if (!name || !email || !password) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing required fields"
    );
  }

  try {
    // 1️⃣ CREATE AUTH USER
    const user = await admin.auth().createUser({
      email: email,
      password: password
    });

    const uid = user.uid;

    // 2️⃣ SET ROLE
    await admin.auth().setCustomUserClaims(uid, {
      role: "FACULTY"
    });

    // 3️⃣ WRITE TO DB
    await admin.database().ref(`faculty/${uid}`).set({
      faculty_id: uid,
      name: name,
      email: email,
      department: department || "",
      role: "FACULTY",
      created_at: admin.database.ServerValue.TIMESTAMP
    });

    // 4️⃣ USERS TABLE
    await admin.database().ref(`users/${uid}`).set({
      role: "FACULTY",
      email: email
    });

    return {
      success: true,
      faculty_id: uid
    };

  } catch (err) {
    throw new functions.https.HttpsError(
      "internal",
      err.message
    );
  }
});
