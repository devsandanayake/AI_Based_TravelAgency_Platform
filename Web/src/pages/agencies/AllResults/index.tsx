import { useRouter } from "next/router";
import Image from "next/image";
import React, { useEffect, useState } from "react";
import NIV from "@/components/niv";
import image4 from "../../../../public/assets/0bc1afa088fef2b0de1c399ea24be955.jpeg";
import { ref, get } from "firebase/database";
import { database } from "../../../../firebaseconfig";
import Modal from "../../../components/Modal";

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

  console.log("Data List:", dataList);

  const handleCardClick = (data: any) => {
    setSelectedData(data);
    setIsModalOpen(true);
  };

  console.log("Selected Data:", selectedData);

  return (
    <main className="flex flex-col h-screen w-screen">
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>

      <div className="flex h-full">
        <div className="w-1/2 h-full">
          <Image
            src={image4}
            alt="Travel Destination"
            className="object-cover w-full h-full"
            priority
          />
        </div>

        <div className="w-1/2 h-full bg-white p-6 overflow-y-auto">
          <h1 className="text-black font-bold text-xl mb-6">Agencies</h1>
          <div className="space-y-4">
            {dataList.length > 0 ? (
              dataList.map((item, index) => (
                <div
                  key={index}
                  className="border p-4 rounded-md cursor-pointer shadow-sm hover:shadow-lg transition"
                  onClick={() => handleCardClick(item)}
                >
                  <p className="text-gray-800 font-semibold">
                    Timestamp: {new Date(item.timestamp).toLocaleString()}
                  </p>
                </div>
              ))
            ) : (
              <p className="text-red-500">No trip data available.</p>
            )}
          </div>
        </div>
      </div>

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
