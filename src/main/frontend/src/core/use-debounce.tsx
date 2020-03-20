import {useRef} from 'react';
import _ from 'lodash';

export function useDebounce<T extends (...args: any[]) => any>(
    callback: T,
    delay: number = 0,
    options?: _.DebounceSettings
): T & _.Cancelable {
    return useRef(_.debounce(callback, delay, options)).current;
}