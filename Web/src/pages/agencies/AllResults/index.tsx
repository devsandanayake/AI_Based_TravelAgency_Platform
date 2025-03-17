import { useRouter } from "next/router";
import Image from "next/image";
import React, { useEffect, useState } from "react";
import NIV from "@/components/niv";
import Modal from "../../../components/Modal";
import { ref, get } from "firebase/database";
import { database } from "../../../../firebaseconfig";

import image4 from "../../../../public/assets/0bc1afa088fef2b0de1c399ea24be955.jpeg";

export default function UploadedResult() {
  const router = useRouter();
  const [dataList, setDataList] = useState<any[]>([]);
  const [selectedData, setSelectedData] = useState<any>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      const id = localStorage.getItem("id");
      if (!id) {
        console.error("User ID not found in localStorage.");
        return;
      }

      const travelAreasRef = ref(database, `travel_agency/${id}`);
      try {
        const snapshot = await get(travelAreasRef);
        if (snapshot.exists()) {
          const data = snapshot.val();
          const formattedData = Object.keys(data).map((key) => ({
            id: key,
            ...data[key],
          }));
          setDataList(formattedData);
        } else {
          console.log("No data available in travel_agency.");
        }
      } catch (error) {
        console.error("Error fetching data from Firebase:", error);
      }
    };
    fetchData();
  }, []);

  const handleCardClick = (data: any) => {
    setSelectedData(data);
    setIsModalOpen(true);
  };

  return (
    <main className="flex flex-col h-screen w-screen">
      {/* Navbar */}
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>

      <div className="flex h-full">
        {/* Left Side Image */}
        <div className="w-full lg:w-1/2 h-screen">
          <Image
            src={image4}
            alt="Travel Destination"
            className="object-cover w-full h-full"
            priority
          />
        </div>

        {/* Right Side List */}
        <div className="w-full lg:w-1/2 flex flex-col items-center bg-gradient-to-b from-blue-200 to-blue-400 overflow-y-auto p-6 h-screen">
          <h1 className="text-3xl font-extrabold text-blue-900 mt-10 mb-6"> Agencies</h1>
          
          <div className="space-y-4 w-full max-w-lg">
            {dataList.length > 0 ? (
              dataList.map((item, index) => (
                <div
                  key={index}
                  className="p-4 rounded-xl bg-white/30 backdrop-blur-md shadow-md cursor-pointer transition duration-300 transform hover:scale-105 hover:bg-white/40"
                  onClick={() => handleCardClick(item)}
                >
                  <p className="text-gray-800 font-semibold">
                    <span className="text-blue-900 font-bold">Timestamp:</span> {new Date(item.timestamp).toLocaleString()}
                  </p>
                </div>
              ))
            ) : (
              <p className="text-red-500 text-lg font-medium text-center">No trip data available.</p>
            )}
          </div>
        </div>
      </div>

      {/* Modal for Agency Details */}
      {isModalOpen && selectedData && (
        <Modal onClose={() => setIsModalOpen(false)}>
          <h2 className="text-black font-bold text-lg mb-3">Recommended Agencies</h2>
          <div className="space-y-2">
            {selectedData.prediction && selectedData.prediction[0] ? (
              <ul className="list-disc pl-5 text-gray-700">
                {selectedData.prediction[0].map((agency: string, index: number) => (
                  <li key={index} className="mb-2">
                    {agency}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-red-500">No predictions available.</p>
            )}
          </div>
        </Modal>
      )}
    </main>
  );
}
