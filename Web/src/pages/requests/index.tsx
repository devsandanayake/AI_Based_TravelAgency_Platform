import React, { useState, useEffect } from "react";
import { ref, get, update, set, push, onValue } from "firebase/database";
import { database } from "../../../firebaseconfig";
import NIV from "@/components/niv";
import {
  FaComment,
  FaCheck,
  FaTrash,
  FaStar,
  FaUser,
  FaClock,
  FaEnvelope,
  FaInbox,
} from "react-icons/fa";

export default function Requests() {
  const [sentRequests, setSentRequests] = useState<any[]>([]);
  const [receivedRequests, setReceivedRequests] = useState<any[]>([]);
  const [selectedChat, setSelectedChat] = useState<any>(null);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [newMessage, setNewMessage] = useState("");
  const [chatMessages, setChatMessages] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState("Sending_Requests");

  useEffect(() => {
    const fetchRequests = async () => {
      const id = localStorage.getItem("id");
      if (!id) return;

      const requestsRef = ref(database, "requests");
      const usersRef = ref(database, "user");

      try {
        const [requestsSnapshot, usersSnapshot] = await Promise.all([
          get(requestsRef),
          get(usersRef),
        ]);

        if (requestsSnapshot.exists()) {
          const requests = requestsSnapshot.val();
          const users = usersSnapshot.val();

          const sent = [];
          const received = [];

          for (const requestId in requests) {
            const request = requests[requestId];
            if (request.fromUserId === id) {
              sent.push({
                id: requestId,
                ...request,
                toUserName: users[request.toUserId]?.uname || "Unknown User",
                friendliness: users[request.toUserId]?.friendliness || 0,
              });
            } else if (request.toUserId === id) {
              received.push({
                id: requestId,
                ...request,
                fromUserName:
                  users[request.fromUserId]?.uname || "Unknown User",
                friendliness: users[request.fromUserId]?.friendliness || 0,
              });
            }
          }

          setSentRequests(sent);
          setReceivedRequests(received);
        }
      } catch (error) {
        console.error("Error fetching requests:", error);
      }
    };

    fetchRequests();
  }, []);

  useEffect(() => {
    if (selectedChat) {
      const chatRef = ref(database, `chats/${selectedChat.id}`);
      const unsubscribe = onValue(chatRef, (snapshot) => {
        if (snapshot.exists()) {
          const messages = snapshot.val();
          const formattedMessages = Object.keys(messages).map((key) => ({
            id: key,
            ...messages[key],
          }));
          setChatMessages(formattedMessages);
        } else {
          setChatMessages([]);
        }
      });

      return () => unsubscribe();
    }
  }, [selectedChat]);

  const handleAccept = async (requestId: string) => {
    try {
      const requestRef = ref(database, `requests/${requestId}`);
      await update(requestRef, { status: "approved" });

      setReceivedRequests((prev) =>
        prev.map((req) =>
          req.id === requestId ? { ...req, status: "approved" } : req
        )
      );
    } catch (error) {
      console.error("Error accepting request:", error);
    }
  };

  const handleDelete = async (requestId: string) => {
    try {
      const requestRef = ref(database, `requests/${requestId}`);
      await update(requestRef, { status: "deleted" });

      setReceivedRequests((prev) =>
        prev.map((req) =>
          req.id === requestId ? { ...req, status: "deleted" } : req
        )
      );
    } catch (error) {
      console.error("Error deleting request:", error);
    }
  };

  const handleSendMessage = async () => {
    if (!selectedChat || !newMessage.trim()) return;

    try {
      const chatRef = ref(database, `chats/${selectedChat.id}`);
      const newMessageRef = push(chatRef);
      await set(newMessageRef, {
        message: newMessage,
        senderId: localStorage.getItem("id"),
        timestamp: new Date().toISOString(),
      });

      setNewMessage("");
    } catch (error) {
      console.error("Error sending message:", error);
    }
  };

  return (
    <main className="min-h-screen flex flex-col bg-gray-50">
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>

      <div className="flex-1 w-full px-4 sm:px-8 lg:px-16 py-8">
        <div className="flex flex-row justify-center gap-6 mb-8">
          <button
            onClick={() => setActiveTab("Sending_Requests")}
            className={`flex items-center p-4 px-8 font-bold rounded-xl transition-all duration-200 ${
              activeTab === "Sending_Requests"
                ? "text-white bg-blue-600 shadow-lg"
                : "text-blue-600 bg-white border-2 border-blue-600 hover:bg-blue-50"
            }`}
          >
            <FaEnvelope className="mr-3 text-xl" />
            Sending Requests
          </button>
          <button
            onClick={() => setActiveTab("Received_Requests")}
            className={`flex items-center p-4 px-8 font-bold rounded-xl transition-all duration-200 ${
              activeTab === "Received_Requests"
                ? "text-white bg-blue-600 shadow-lg"
                : "text-blue-600 bg-white border-2 border-blue-600 hover:bg-blue-50"
            }`}
          >
            <FaInbox className="mr-3 text-xl" />
            Received Requests
          </button>
        </div>

        <div className="w-full">
          {activeTab === "Sending_Requests" && (
            <div className="bg-white rounded-xl p-8 shadow-lg border-2 border-blue-100">
              <h2 className="text-2xl font-bold text-blue-600 mb-8 flex items-center">
                <FaEnvelope className="mr-3 text-xl" />
                Sending Requests
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {sentRequests.length === 0 ? (
                  <div className="col-span-full text-center py-12 text-gray-500">
                    <FaEnvelope className="mx-auto text-4xl mb-4 text-blue-200" />
                    <p className="text-xl font-medium">No sent requests yet</p>
                    <p className="text-sm mt-2">
                      Your sent requests will appear here
                    </p>
                  </div>
                ) : (
                  sentRequests.map((request) => (
                    <div
                      key={request.id}
                      className="bg-blue-50 p-6 rounded-xl hover:shadow-md transition-shadow duration-200"
                    >
                      <div className="flex flex-col h-full">
                        <div className="flex-1">
                          <div className="flex items-center mb-3">
                            <FaUser className="text-blue-600 mr-3 text-xl" />
                            <p className="font-semibold text-lg">
                              {request.toUserName}
                            </p>
                          </div>
                          <div className="flex items-center text-sm text-gray-600">
                            <FaClock className="mr-3" />
                            <p>
                              Status:{" "}
                              <span
                                className={`font-medium ${
                                  request.status === "approved"
                                    ? "text-green-600"
                                    : request.status === "pending"
                                    ? "text-yellow-600"
                                    : "text-red-600"
                                }`}
                              >
                                {request.status}
                              </span>
                            </p>
                          </div>
                        </div>
                        {request.status === "approved" && (
                          <div className="mt-4 flex justify-end">
                            <button
                              onClick={() => {
                                setSelectedChat(request);
                                setIsChatOpen(true);
                              }}
                              className="p-4 text-blue-600 hover:text-blue-800 bg-white rounded-full shadow-sm hover:shadow-md transition-all duration-200"
                              title="Start Chat"
                            >
                              <FaComment size={24} />
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === "Received_Requests" && (
            <div className="bg-white rounded-xl p-8 shadow-lg border-2 border-blue-100">
              <h2 className="text-2xl font-bold text-blue-600 mb-8 flex items-center">
                <FaInbox className="mr-3 text-xl" />
                Received Requests
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {receivedRequests.length === 0 ? (
                  <div className="col-span-full text-center py-12 text-gray-500">
                    <FaInbox className="mx-auto text-4xl mb-4 text-blue-200" />
                    <p className="text-xl font-medium">
                      No received requests yet
                    </p>
                    <p className="text-sm mt-2">
                      Requests from other users will appear here
                    </p>
                  </div>
                ) : (
                  receivedRequests.map((request) => (
                    <div
                      key={request.id}
                      className="bg-blue-50 p-6 rounded-xl hover:shadow-md transition-shadow duration-200"
                    >
                      <div className="flex flex-col h-full">
                        <div className="flex-1">
                          <div className="flex items-center mb-3">
                            <FaUser className="text-blue-600 mr-3 text-xl" />
                            <p className="font-semibold text-lg">
                              {request.fromUserName}
                            </p>
                          </div>
                          <div className="flex items-center text-sm text-gray-600">
                            <FaClock className="mr-3" />
                            <p>
                              Status:{" "}
                              <span
                                className={`font-medium ${
                                  request.status === "approved"
                                    ? "text-green-600"
                                    : request.status === "pending"
                                    ? "text-yellow-600"
                                    : "text-red-600"
                                }`}
                              >
                                {request.status}
                              </span>
                            </p>
                          </div>
                        </div>
                        <div className="mt-4 flex justify-end space-x-3">
                          {request.status === "pending" && (
                            <>
                              <button
                                onClick={() => handleAccept(request.id)}
                                className="p-4 text-green-600 hover:text-green-800 bg-white rounded-full shadow-sm hover:shadow-md transition-all duration-200"
                                title="Accept Request"
                              >
                                <FaCheck size={24} />
                              </button>
                              <button
                                onClick={() => handleDelete(request.id)}
                                className="p-4 text-red-600 hover:text-red-800 bg-white rounded-full shadow-sm hover:shadow-md transition-all duration-200"
                                title="Delete Request"
                              >
                                <FaTrash size={24} />
                              </button>
                            </>
                          )}
                          {request.status === "approved" && (
                            <button
                              onClick={() => {
                                setSelectedChat(request);
                                setIsChatOpen(true);
                              }}
                              className="p-4 text-blue-600 hover:text-blue-800 bg-white rounded-full shadow-sm hover:shadow-md transition-all duration-200"
                              title="Start Chat"
                            >
                              <FaComment size={24} />
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </div>
      {isChatOpen && selectedChat && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-4xl max-h-[90vh] flex flex-col">
            <div className="flex justify-between items-center mb-4 pb-4 border-b">
              <h3 className="text-xl font-bold text-blue-600 flex items-center">
                <FaUser className="mr-3 text-xl" />
                Chat with{" "}
                {selectedChat.fromUserId === localStorage.getItem("id")
                  ? selectedChat.toUserName
                  : selectedChat.fromUserName}
              </h3>
              <button
                onClick={() => setIsChatOpen(false)}
                className="text-gray-500 hover:text-gray-700 p-2 rounded-full hover:bg-gray-100"
              >
                ×
              </button>
            </div>
            <div className="flex-1 overflow-y-auto mb-4 space-y-4 p-4">
              {chatMessages.length === 0 ? (
                <div className="text-center text-gray-500 py-12">
                  <FaComment className="mx-auto text-4xl mb-4 text-blue-200" />
                  <p className="text-xl font-medium">No messages yet</p>
                  <p className="text-sm mt-2">Start the conversation!</p>
                </div>
              ) : (
                chatMessages.map((message) => {
                  const isCurrentUser =
                    message.senderId === localStorage.getItem("id");
                  return (
                    <div
                      key={message.id}
                      className={`flex ${
                        isCurrentUser ? "justify-end" : "justify-start"
                      }`}
                    >
                      <div
                        className={`max-w-[70%] p-4 rounded-xl ${
                          isCurrentUser
                            ? "bg-blue-600 text-white rounded-br-none"
                            : "bg-gray-200 text-gray-800 rounded-bl-none"
                        }`}
                      >
                        <p className="text-sm">{message.message}</p>
                        <p className="text-xs mt-2 opacity-70">
                          {new Date(message.timestamp).toLocaleTimeString()}
                        </p>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
            <div className="flex gap-3 p-4 border-t">
              <input
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onKeyPress={(e) => e.key === "Enter" && handleSendMessage()}
                placeholder="Type your message..."
                className="flex-1 p-4 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={handleSendMessage}
                className="bg-blue-600 text-white px-8 py-4 rounded-xl hover:bg-blue-700 transition-colors duration-200"
              >
                Send
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
