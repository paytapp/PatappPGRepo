import React from "react";

export default function useWindowSize() {
  const isSSR = typeof window !== "undefined";
  const [windowSize, setWindowSize] = React.useState({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  function changeWindowSize() {
    setWindowSize({ width: window.innerWidth, height: window.innerHeight });
  }

  React.useEffect(() => {
    window.addEventListener("resize", changeWindowSize);

    return () => {
      window.removeEventListener("resize", changeWindowSize);
    };
  }, []);

  return windowSize;
}


// import { useEffect, useState } from 'react';

// export  default function useWindowSize() {
//   const [width, setWidth] = useState(window.innerWidth);

//   useEffect(() => {
//     const handler = (event) => {      
//       setWidth(event.target.innerWidth);
//     };

//     window.addEventListener('resize', handler);

//     return () => {
//       window.removeEventListener('resize', handler);
//     };
//   }, []);

//   return width;
// }