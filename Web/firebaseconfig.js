import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
  apiKey: "AIzaSyBHcdoBlGAs80djZ6FiKEoXtc3GBn0wWew",
  authDomain: "travelling-e138d.firebaseapp.com",
  databaseURL: "https://travelling-e138d-default-rtdb.firebaseio.com",
  projectId: "travelling-e138d",
  storageBucket: "travelling-e138d.appspot.com",
  messagingSenderId: "684264279537",
  appId: "1:684264279537:web:b209a7556ab4c31cfbc850",
};

const app = initializeApp(firebaseConfig);
const database = getDatabase(app);
const auth = getAuth(app);

export { app, auth, database };
