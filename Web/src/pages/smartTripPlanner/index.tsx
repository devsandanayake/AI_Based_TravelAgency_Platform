import Image from "next/image";
import { useState } from "react";
import axios from "axios";
import { ref, push, set } from "firebase/database";
import { app, database } from "../../../firebaseconfig";
import NIV from "@/components/niv";
import image3 from "../../../public/assets/6.jpg";
import Swal from "sweetalert2";
import { useRouter } from "next/router";
import image0 from "../../../public/assets/polgaha.png";

export default function Destination() {
  const [formData, setFormData] = useState({
    destination: "",
    month: "",
    travelduration: "",
    activities: "",
    specialrequirements: "",
  });

  const router = useRouter();
  const [showOptions, setShowOptions] = useState(false);
  let Climate = "";

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    setShowOptions(false);
  };

  if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "January"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "February"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "March"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "April"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "May"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "June"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "July"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "August"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "September"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "October"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "November"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Trincomalee, Sri Lanka" &&
    formData.month == "December"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "January"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "February"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "March"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "April"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "May"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "June"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "July"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "August"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "September"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "October"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "November"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Nuwara Eliya, Sri Lanka" &&
    formData.month == "December"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "January"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "February"
  ) {
    Climate = "Cloudy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "March"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "April"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "May"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "June"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "July"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "August"
  ) {
    Climate = "Rainy";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "September"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "October"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "November"
  ) {
    Climate = "Sunny";
  } else if (
    formData.destination == "Mirissa, Sri Lanka" &&
    formData.month == "December"
  ) {
    Climate = "Cloudy";
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const transformedData = {
      "Destination": formData.destination,
      "Month": formData.month,
      "Travel Duration": formData.travelduration,
      "Climate": Climate,
      "Activites": formData.activities,
      "Special Requirements": formData.specialrequirements,
    };

    console.log(transformedData);

    Swal.fire({
      title: "Processing...",
      html: 'Please wait while the data is being processed.<br><div class="spinner-border" role="status"></div>',
      allowOutsideClick: false,
      showCancelButton: false,
      showConfirmButton: false,
    });

    try {
      const response = await axios.post(
        "https://smarttripplannerupdated-357877744933.us-central1.run.app",
        transformedData
      );

      console.log("API Response:", response);

      const id = localStorage.getItem("id");
      if (!id) {
        console.error("User ID not found in localStorage");
        Swal.fire("Error", "User ID not found", "error");
        return;
      }

      console.log("User ID:", id);
      console.log("Response Data:", response.data);

      const dataToStore = {
        ...response.data,
        timestamp: new Date().toISOString(),
      };

      const tripRef = ref(database, `travelAreas/${id}`);
      await push(tripRef, dataToStore);

      Swal.fire({
        title: "Success!",
        text: "Smart Trip Created Successfully!",
        icon: "success",
        confirmButtonText: "OK",
      }).then(() => {
        router.push({
          pathname: "/smartTripPlanner/UploadedResult",
          query: { data: JSON.stringify(response.data) },
        });
      });
    } catch (error) {
      console.error("Error submitting data:", error);
      Swal.fire(
        "Error",
        "Failed to create smart trip. Please try again.",
        "error"
      );
    }
  };

  return (
    <main className="flex flex-col h-screen w-screen">
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>
      <div className="flex flex-col lg:flex-row h-full relative">
        <div className="w-full lg:w-1/2 h-screen">
          <Image
            className="object-cover w-full h-full"
            src={image3}
            alt="Login Image"
            priority
          />
        </div>
        <div className="w-full lg:w-1/2 ml-auto flex flex-col items-center bg-gradient-to-b from-blue-200 to-blue-400 overflow-y-auto p-6 h-screen">
          <div className="relative w-full text-center mb-8">
            <h1 className="text-2xl lg:text-4xl font-semibold text-black mt-14">
              SUGGEST DESTINATION PLAN
            </h1>
            <button
              onClick={() => router.push("/smartTripPlanner/AllResults")}
              className="absolute top-1 right-2 flex items-center gap-2 py-2 px-3 bg-indigo-600 text-white rounded-t-lg hover:bg-indigo-700"
            >
              <Image src={image0} alt="Icon" width={20} height={20} />
              Planners
            </button>
          </div>
          <h5 className="text-center text-zinc-600 font-semibold text-sm leading-tight font-poppins mb-8">
            Fill out the following form to get your recommended plans in Sri
            Lanka.
          </h5>
          <form onSubmit={handleSubmit} className="space-y-4 w-full max-w-lg">
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label className="text-gray-600" htmlFor="destination">
                  Destination
                </label>
                <input
                  type="text"
                  id="destination"
                  name="destination"
                  required
                  value={formData.destination}
                  onChange={handleChange}
                  placeholder="Enter your destination"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="month" className="text-gray-600">
                  Month
                </label>
                <select
                  id="month"
                  name="month"
                  required
                  value={formData.month}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Month
                  </option>
                  <option value="January">January</option>
                  <option value="February">February</option>
                  <option value="March">March</option>
                  <option value="April">April</option>
                  <option value="May">May</option>
                  <option value="June">June</option>
                  <option value="July">July</option>
                  <option value="August">August</option>
                  <option value="September">September</option>
                  <option value="October">October</option>
                  <option value="November">November</option>
                  <option value="December">December</option>
                </select>
              </div>
            </div>

            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="travelduration" className="text-gray-600">
                  Duration
                </label>
                <select
                  id="travelduration"
                  name="travelduration"
                  required
                  value={formData.travelduration}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Duration
                  </option>
                  <option value="1 Day">1 Day</option>
                  <option value="2 Days">2 Days</option>
                  <option value="3 Days">3 Days</option>
                  <option value="4 Days">4 Days</option>
                  <option value="5 Days">5 Days</option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="specialrequirements" className="text-gray-600">
                  Special Requirements
                </label>
                <input
                  type="text"
                  id="specialrequirements"
                  name="specialrequirements"
                  required
                  value={formData.specialrequirements}
                  onChange={handleChange}
                  placeholder="Enter special requirements"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full">
                <label htmlFor="activities" className="text-gray-600">
                  Activities
                </label>
                <input
                  type="text"
                  id="activities"
                  name="activities"
                  required
                  value={formData.activities}
                  onChange={handleChange}
                  placeholder="Enter activities or interests"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <button
              type="submit"
              className="w-full py-2 px-4 rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              Submit
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}
