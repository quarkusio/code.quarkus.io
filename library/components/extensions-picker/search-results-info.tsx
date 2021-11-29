import React, { SetStateAction } from 'react';
import { clearFilterOrigin, FilterResult } from './extensions-utils';
import classNames from 'classnames';
import { FaCheck } from 'react-icons/fa';
import { Button } from 'react-bootstrap';
import './search-results-info.scss';

export function SearchResultsInfo(props: { filter: string; setFilter: React.Dispatch<SetStateAction<string>>, result: FilterResult }) {
  function clickPlatform() {
    if (props.result.origin !== 'platform') {
      props.setFilter(`${clearFilterOrigin(props.filter)}`);
    } else {
      props.setFilter(`origin:any ${clearFilterOrigin(props.filter)}`);
    }
  }
  function clickOther() {
    if (props.result.origin !== 'other' ) {
      props.setFilter(`origin:other ${clearFilterOrigin(props.filter)}`);
    } else {
      props.setFilter(`origin:any ${clearFilterOrigin(props.filter)}`);
    }
  }
  function clearFilter() {
    props.setFilter('');
  }
  const originOther =  props.result?.origin === 'other' ;
  const originPlatform =  props.result?.origin === 'platform';
  return (
    <>
      {props.result?.filtered && (
        <div className="search-results-info">
          {props.result.any.length === 0 && <b>No extensions found </b>}
          {props.result.any.length > 0 && (
            <span className='origins-count'>
              <span className='results'>Extensions found by origin: </span>
              <span className={classNames('origin-count', 'platform-origin', { 'current-origin': originPlatform })} onClick={clickPlatform}>
                {originPlatform && <FaCheck />}<span className='count'>{props.result.platform.length}</span> Platform
              </span>
              <span className={classNames('origin-count', 'other-origin', { 'current-origin': originOther })} onClick={clickOther}>
                {originOther && <FaCheck />}<span className='count'>{props.result.other.length}</span> Other
              </span>
            </span>
          )}
          <Button as="a" className='clear-button' onClick={clearFilter}>Clear filter</Button>
        </div>
      )}
    </>
  )
}
