import { useEffect, useRef } from 'react';

function debounce(container: { fn: any }, wait: number, immediate: boolean) {
  let timeout: number | null;

  return function executedFunction() {
    // @ts-ignore
    const context = this;
    const args = arguments;

    const callNow = immediate && !timeout;

    if (timeout) {
      window.clearTimeout(timeout);
    }

    const later = function () {
      timeout = null;
      if (!callNow) container.fn.apply(context, args);
    };

    timeout = window.setTimeout(later, wait);

    if (callNow) container.fn.apply(context, args);
  };
}

export function useDebounce<T extends (...args: any[]) => any>(
  callback?: T,
  delay: number = 0,
): T | undefined {
  const containerRef = useRef({ fn: callback });
  const debounceRef = useRef(callback && debounce(containerRef.current, delay, true));
  useEffect(() => {
    if(containerRef.current.fn !== callback) {
      containerRef.current.fn = callback;
    }
  }, [callback, containerRef.current.fn]);
  // @ts-ignore
  return debounceRef.current;
}