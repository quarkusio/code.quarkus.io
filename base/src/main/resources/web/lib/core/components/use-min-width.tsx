import { useEffect, useState } from 'react';

const checkMinWidth = (minWidth: number) => window.innerWidth <= minWidth;

export default function useMinWidth(minWidth: number = 900) {
  const [checkResult, setCheckResult] = useState(checkMinWidth(minWidth));

  useEffect(() => {
    const onResize = () => {
      setCheckResult(checkMinWidth(minWidth));
    }

    window.addEventListener("resize", onResize);

    return () => {
      window.removeEventListener("resize", onResize);
    }
  }, []);

  return checkResult;
}