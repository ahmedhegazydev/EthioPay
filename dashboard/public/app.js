import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import {
  browserLocalPersistence,
  getAuth,
  onAuthStateChanged,
  setPersistence,
  signInWithEmailAndPassword,
  signOut,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";
import {
  collection,
  doc,
  getFirestore,
  onSnapshot,
  setDoc,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyBLjwByAvFHBD1CBEL4jnhZpZ7yppZjGa0",
  authDomain: "ethiopay-542c4.firebaseapp.com",
  projectId: "ethiopay-542c4",
  storageBucket: "ethiopay-542c4.firebasestorage.app",
  messagingSenderId: "647858905848",
  appId: "1:647858905848:web:f741fc3da2be168c1f910d",
  measurementId: "G-6WMZFTL966",
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

const state = {
  users: [],
  wallets: [],
  transactions: [],
  events: [],
  unsubscribers: [],
};

const elements = {
  loginView: document.getElementById("loginView"),
  dashboardView: document.getElementById("dashboardView"),
  loginForm: document.getElementById("loginForm"),
  emailInput: document.getElementById("emailInput"),
  passwordInput: document.getElementById("passwordInput"),
  loginButton: document.getElementById("loginButton"),
  refreshButton: document.getElementById("refreshButton"),
  logoutButton: document.getElementById("logoutButton"),
  connectionPill: document.getElementById("connectionPill"),
  usersCount: document.getElementById("usersCount"),
  walletBalance: document.getElementById("walletBalance"),
  transactionsCount: document.getElementById("transactionsCount"),
  pendingKycCount: document.getElementById("pendingKycCount"),
  usersTableBody: document.getElementById("usersTableBody"),
  transactionsList: document.getElementById("transactionsList"),
  eventsList: document.getElementById("eventsList"),
  banner: document.getElementById("banner"),
};

function showBanner(message, kind = "info") {
  elements.banner.textContent = message;
  elements.banner.className = `banner ${kind}`;
  elements.banner.classList.remove("hidden");
  window.clearTimeout(showBanner.timeout);
  showBanner.timeout = window.setTimeout(() => {
    elements.banner.className = "banner hidden";
  }, 4200);
}

function setConnection(label, kind = "info") {
  elements.connectionPill.textContent = label;
  elements.connectionPill.className = "status-pill";
  if (kind === "error") {
    elements.connectionPill.style.background = "rgba(179, 38, 30, 0.12)";
    elements.connectionPill.style.color = "#b3261e";
  } else if (kind === "success") {
    elements.connectionPill.style.background = "rgba(46, 125, 50, 0.12)";
    elements.connectionPill.style.color = "#2e7d32";
  } else {
    elements.connectionPill.style.background = "rgba(26, 86, 190, 0.08)";
    elements.connectionPill.style.color = "#1a56be";
  }
}

function formatMoney(value) {
  return `${new Intl.NumberFormat("en-US").format(Number(value) || 0)} ETB`;
}

function toMillis(value) {
  if (typeof value === "number") return value;
  if (typeof value === "string") {
    const parsed = Date.parse(value);
    return Number.isNaN(parsed) ? 0 : parsed;
  }
  if (value && typeof value.seconds === "number") {
    return value.seconds * 1000;
  }
  return 0;
}

function formatDate(value) {
  const millis = toMillis(value);
  if (!millis) return "No date";
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(millis);
}

function getWalletForUser(userId) {
  return state.wallets.find((wallet) => wallet.userId === userId || wallet.id === userId);
}

function sortByNewest(items, key) {
  return [...items].sort((left, right) => toMillis(right[key]) - toMillis(left[key]));
}

function renderSummary() {
  const walletTotal = state.wallets.reduce((sum, wallet) => sum + Number(wallet.balance || 0), 0);
  const pendingKyc = state.users.filter((user) => !user.kycApproved).length;

  elements.usersCount.textContent = state.users.length.toString();
  elements.walletBalance.textContent = formatMoney(walletTotal);
  elements.transactionsCount.textContent = state.transactions.length.toString();
  elements.pendingKycCount.textContent = pendingKyc.toString();
}

function renderUsers() {
  const sortedUsers = sortByNewest(state.users, "updatedAt").slice(0, 50);

  if (!sortedUsers.length) {
    elements.usersTableBody.innerHTML = `
      <tr>
        <td colspan="6">No users found yet. Once the mobile app signs users in, they'll appear here live.</td>
      </tr>
    `;
    return;
  }

  elements.usersTableBody.innerHTML = sortedUsers
    .map((user) => {
      const wallet = getWalletForUser(user.id);
      const kycClass = user.kycApproved ? "success" : "warn";
      const freezeClass = user.isFrozen ? "danger" : "info";
      const freezeLabel = user.isFrozen ? "Frozen" : "Active";
      const approveDisabled = user.kycApproved ? "disabled" : "";
      const freezeActionClass = user.isFrozen ? "" : "danger";
      const freezeActionLabel = user.isFrozen ? "Unfreeze" : "Freeze";

      return `
        <tr>
          <td>
            <div class="user-title">
              <strong>${user.displayName || "Noble User"}</strong>
              <span class="user-meta">${user.email || user.id}</span>
            </div>
          </td>
          <td>${user.phone || user.phoneNormalized || "—"}</td>
          <td>${wallet ? formatMoney(wallet.balance) : "0 ETB"}</td>
          <td>
            <div class="badge-row">
              <span class="badge ${kycClass}">${user.kycApproved ? "Approved" : "Pending"}</span>
            </div>
          </td>
          <td>
            <div class="badge-row">
              <span class="badge ${freezeClass}">${freezeLabel}</span>
            </div>
          </td>
          <td>
            <div class="action-row">
              <button class="table-action primary" data-action="approve-kyc" data-user-id="${user.id}" ${approveDisabled}>Approve KYC</button>
              <button class="table-action ${freezeActionClass}" data-action="toggle-freeze" data-user-id="${user.id}">
                ${freezeActionLabel}
              </button>
            </div>
          </td>
        </tr>
      `;
    })
    .join("");
}

function renderTransactions() {
  const items = sortByNewest(state.transactions, "createdAt").slice(0, 12);
  elements.transactionsList.innerHTML = items.length
    ? items
        .map(
          (transaction) => `
            <article class="stack-item">
              <strong>${formatMoney(transaction.amount)} • ${transaction.status || "completed"}</strong>
              <div class="stack-meta">From <strong>${transaction.fromUserId}</strong> to <strong>${transaction.toUserId}</strong></div>
              <div class="stack-meta">${transaction.note || "No note"} • ${formatDate(transaction.createdAt)}</div>
            </article>
          `,
        )
        .join("")
    : `<article class="stack-item"><strong>No transactions yet</strong><div class="stack-meta">Transfers and top-ups will appear here automatically.</div></article>`;
}

function renderEvents() {
  const items = sortByNewest(state.events, "createdAt").slice(0, 12);
  elements.eventsList.innerHTML = items.length
    ? items
        .map(
          (event) => `
            <article class="stack-item">
              <strong>${event.action || "event"}</strong>
              <div class="stack-meta">Target: ${event.targetId || "—"}</div>
              <div class="stack-meta">${event.meta || "No metadata"} • ${formatDate(event.createdAt)}</div>
            </article>
          `,
        )
        .join("")
    : `<article class="stack-item"><strong>No admin events yet</strong><div class="stack-meta">Approve KYC or freeze users to generate an audit trail.</div></article>`;
}

function renderAll() {
  renderSummary();
  renderUsers();
  renderTransactions();
  renderEvents();
}

function clearSubscriptions() {
  state.unsubscribers.forEach((unsubscribe) => unsubscribe());
  state.unsubscribers = [];
}

function subscribeToCollection(name, path, sortField, defaultLabel) {
  const unsubscribe = onSnapshot(
    collection(db, path),
    (snapshot) => {
      state[name] = sortByNewest(
        snapshot.docs.map((documentSnapshot) => ({
          id: documentSnapshot.id,
          ...documentSnapshot.data(),
        })),
        sortField,
      );
      renderAll();
      setConnection("Live sync connected", "success");
    },
    (error) => {
      console.error(`${defaultLabel} listener failed`, error);
      setConnection("Partial sync issue", "error");
      showBanner(`${defaultLabel} failed to sync: ${error.message}`, "error");
    },
  );

  state.unsubscribers.push(unsubscribe);
}

function startRealtimeSync() {
  clearSubscriptions();
  setConnection("Connecting to Firebase...");
  subscribeToCollection("users", "users", "updatedAt", "Users");
  subscribeToCollection("wallets", "wallets", "updatedAt", "Wallets");
  subscribeToCollection("transactions", "transactions", "createdAt", "Transactions");
  subscribeToCollection("events", "adminEvents", "createdAt", "Admin events");
}

async function writeAdminEvent(action, targetId, meta) {
  const eventRef = doc(collection(db, "adminEvents"));
  await setDoc(eventRef, {
    id: eventRef.id,
    action,
    targetId,
    meta,
    createdAt: Date.now(),
  });
}

async function approveKyc(userId) {
  await updateDoc(doc(db, "users", userId), {
    kycApproved: true,
    kycStatus: "approved",
    updatedAt: Date.now(),
  });
  await writeAdminEvent("dashboard_kyc_approved", userId, "Approved from Noble dashboard");
}

async function toggleFreeze(userId) {
  const user = state.users.find((item) => item.id === userId);
  const nextFrozen = !Boolean(user?.isFrozen);

  await updateDoc(doc(db, "users", userId), {
    isFrozen: nextFrozen,
    updatedAt: Date.now(),
  });
  await writeAdminEvent(
    nextFrozen ? "dashboard_user_frozen" : "dashboard_user_unfrozen",
    userId,
    `Frozen state changed to ${nextFrozen}`,
  );
}

elements.loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  elements.loginButton.disabled = true;
  elements.loginButton.textContent = "Signing In...";

  try {
    await setPersistence(auth, browserLocalPersistence);
    await signInWithEmailAndPassword(
      auth,
      elements.emailInput.value.trim(),
      elements.passwordInput.value,
    );
    showBanner("Dashboard signed in successfully.", "success");
  } catch (error) {
    console.error("Sign-in failed", error);
    showBanner(error.message || "Firebase sign-in failed.", "error");
  } finally {
    elements.loginButton.disabled = false;
    elements.loginButton.textContent = "Sign In";
  }
});

elements.logoutButton.addEventListener("click", async () => {
  try {
    await signOut(auth);
    showBanner("Signed out.", "success");
  } catch (error) {
    showBanner(error.message || "Unable to sign out.", "error");
  }
});

elements.refreshButton.addEventListener("click", () => {
  renderAll();
  showBanner("Dashboard snapshot refreshed locally.", "success");
});

elements.usersTableBody.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;

  const action = button.dataset.action;
  const userId = button.dataset.userId;
  if (!action || !userId) return;

  button.disabled = true;
  try {
    if (action === "approve-kyc") {
      await approveKyc(userId);
      showBanner("KYC approved successfully.", "success");
    }
    if (action === "toggle-freeze") {
      await toggleFreeze(userId);
      showBanner("User status updated.", "success");
    }
  } catch (error) {
    console.error("User action failed", error);
    showBanner(error.message || "Action failed.", "error");
  } finally {
    button.disabled = false;
  }
});

onAuthStateChanged(auth, (user) => {
  if (!user) {
    clearSubscriptions();
    elements.loginView.classList.remove("hidden");
    elements.dashboardView.classList.add("hidden");
    setConnection("Signed out");
    return;
  }

  elements.loginView.classList.add("hidden");
  elements.dashboardView.classList.remove("hidden");
  setConnection(`Signed in as ${user.email || user.uid}`);
  startRealtimeSync();
});
