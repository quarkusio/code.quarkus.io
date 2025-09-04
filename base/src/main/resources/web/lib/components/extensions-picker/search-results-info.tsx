import React, {SetStateAction} from 'react';
import {FilterResult} from './extensions-utils';
import './search-results-info.scss';

export function SearchResultsInfo(props: { filter: string; setFilter: React.Dispatch<SetStateAction<string>>, result: FilterResult }) {

  return (
    <>
      {props.result?.filtered && (
        <>
          <div className="search-results-info">
            {props.result.entries.length === 0 && <b>No extensions found </b>}
            {props.result.entries.length > 0 && (
              <span className='result-count'>
                <span className='count'>{props.result.entries.length}</span>
                <span className='info'>extensions</span>
              </span>
            )}

          </div>
        </>
      )}
    </>
  )
}
