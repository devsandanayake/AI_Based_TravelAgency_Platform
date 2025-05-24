import { useRouter } from "next/router";
import Image from "next/image";
import React, { useEffect, useState } from "react";
import NIV from "@/components/niv";
import image4 from "../../../../public/assets/0bc1afa088fef2b0de1c399ea24be955.jpeg";
import Modal from "../../../components/Modal";
import { ref, get, child } from "firebase/database";
import { database } from "../../../../firebaseconfig";

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

      const travelAreasRef = ref(database, `travelAreas/${id}`);
      try {
        const snapshot = await get(travelAreasRef);
        if (snapshot.exists()) {
          const data = snapshot.val();
          const formattedData = Object.keys(data).map((key) => ({
            id: key,
            ...data[key],
          }));
          setDataList(formattedData);
          console.log(formattedData);
        } else {
          console.log("No data available in travelAreas.");
        }
      } catch (error) {
        console.error("Error fetching data from Firebase:", error);
      }
    };
    fetchData();
  }, []);

  const handleCardClick = (data: any) => {
    setSelectedData(data);
    console.log(data);
    setIsModalOpen(true);
  };

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
          <h1 className="text-black font-bold text-xl mb-6">
            Smart Trip Plans
          </h1>
          <div className="space-y-4">
            {dataList.length > 0 ? (
              dataList.map((item, index) => (
                <div
                  key={index}
                  className="border p-4 rounded-md cursor-pointer shadow-sm hover:shadow-lg transition"
                  onClick={() => handleCardClick(item)}
                >
                  <div className="flex justify-between items-center">
                    <p className="text-gray-800 font-semibold">
                      {new Date(item.timestamp).toLocaleString()}
                    </p>
                    <div className="flex gap-4">
                      <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">
                        {item.totalDuration}
                      </span>
                      <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm">
                        {item.month}
                      </span>
                    </div>
                  </div>
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
          <h2 className="text-black font-bold text-lg mb-3">
            Recommended Trip Plan
          </h2>
          <div className="space-y-5 text-gray-700">
            {selectedData.destinations && selectedData.destinations.length > 0 ? (
              selectedData.destinations.map((destination: any, index: number) => (
                <div key={index} className="p-4 border rounded-md bg-gray-50">
                  <h3 className="text-xl font-bold text-blue-600 mb-3">
                    {selectedData.destinationsOrder[`Destination ${index + 1}`]}
                  </h3>
                  {destination.prediction ? (
                    (() => {
                      const tripDays = destination.prediction.match(
                        /Day\s\d+:[\s\S]*?(?=Day\s\d+:|$)/g
                      ) || [];

                      return tripDays.length > 0 ? (
                        tripDays.map((day: string, dayIndex: number) => {
                          const lines = day.trim().split("\n");
                          return (
                            <div
                              key={dayIndex}
                              className="p-4 border rounded-md bg-white mb-4"
                            >
                              {lines.map((line: string, i: number) => {
                                if (/^Day\s\d+:/.test(line)) {
                                  return (
                                    <p
                                      key={i}
                                      className="text-xl font-bold text-blue-600"
                                    >
                                      {line}
                                    </p>
                                  );
                                } else if (
                                  /^Morning$|^Afternoon$|^Evening$/.test(
                                    line.trim()
                                  )
                                ) {
                                  return (
                                    <p
                                      key={i}
                                      className="text-lg font-semibold underline mt-3"
                                    >
                                      {line}
                                    </p>
                                  );
                                } else {
                                  return (
                                    <p key={i} className="text-gray-700">
                                      {line.trim()}
                                    </p>
                                  );
                                }
                              })}
                            </div>
                          );
                        })
                      ) : (
                        <p className="text-red-500">No trip details available.</p>
                      );
                    })()
                  ) : (
                    <p className="text-red-500">No trip plan available.</p>
                  )}
                </div>
              ))
            ) : (
              <p className="text-red-500">No destinations available.</p>
            )}
          </div>
        </Modal>
      )}
    </main>
  );
}
