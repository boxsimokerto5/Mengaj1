const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read students or records", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("students").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("records").get());
});

test("Authenticated user: can create and read their own student", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const studentRef = aliceDb.collection("users").doc(ALICE_UID).collection("students").doc("student_01");

  await assertSucceeds(
    studentRef.set({
      userId: ALICE_UID,
      name: "Ahmad Fauzi",
      gender: "Laki-laki",
      roomOrClass: "Asrama Al-Ikhlas",
      currentType: "IQRO",
      currentLevel: 3,
      currentPage: 15,
      notes: "Perlu bimbingan makhraj ra",
      createdAt: new Date(),
    })
  );

  const snap = await assertSucceeds(studentRef.get());
});

test("Cross-user access: Bob cannot read Alice's students", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("students").doc("student_alice").set({
      userId: ALICE_UID,
      name: "Siti Rahma",
      currentType: "QURAN",
      currentLevel: 5,
      currentPage: 120,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("students").doc("student_alice").get());
});

test("Validation: rejects student with invalid level > 30", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const studentRef = aliceDb.collection("users").doc(ALICE_UID).collection("students").doc("student_invalid");

  await assertFails(
    studentRef.set({
      userId: ALICE_UID,
      name: "Budi Santoso",
      currentType: "IQRO",
      currentLevel: 99, // invalid level > 30
      currentPage: 1,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create ngaji record for a student", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const recordRef = aliceDb.collection("users").doc(ALICE_UID).collection("records").doc("rec_01");

  await assertSucceeds(
    recordRef.set({
      userId: ALICE_UID,
      studentId: "student_01",
      studentName: "Ahmad Fauzi",
      type: "IQRO",
      level: 3,
      page: 15,
      status: "LANCAR",
      date: "2026-10-23",
      notes: "Makhraj sudah bagus",
      teacherName: "Ustadz Abdullah",
      createdAt: new Date(),
    })
  );
});
