import { useEffect, useState } from "react";
import dynamic from "next/dynamic";

const ReactPlayer = dynamic(() => import("react-player/lazy"), { ssr: false });

const VideoComponent = () => {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  return (
    <div className="h-[50vh] w-[90vh] m-20">
      {isClient && (
        <ReactPlayer
          url="https://www.youtube.com/watch?v=ZToicYcHIOU"
          playing
          loop
          muted
          width="100%"
          height="100%"
          className="background-video"
        />
      )}
    </div>
  );
};

export default VideoComponent;
