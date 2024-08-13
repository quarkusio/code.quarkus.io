import React, { SetStateAction } from 'react';
import { clearFilterOrigin, FilterResult } from './extensions-utils';
import classNames from 'classnames';
import { FaCheck } from 'react-icons/fa';
import { Alert, Button } from 'react-bootstrap';
import './search-results-info.scss';
import { DEFAULT_FILTER } from '../api/quarkus-project-utils';
import { useAnalytics } from '../../core/analytics';
import { Plural } from '../../core/components';
import { PlatformIcon } from "./extensions-origin";

export function SearchResultsInfo(props: { filter: string; setFilter: React.Dispatch<SetStateAction<string>>, result: FilterResult }) {
  const analytics = useAnalytics();
  const context = { element: 'search-result-info-panel' };
  function clickPlatform() {
    if (props.result.origin !== 'platform') {
      analytics.event('Switch origin', { origin: 'platform', ...context });
      props.setFilter(`origin:platform ${clearFilterOrigin(props.filter)}`);
    } else {
      analytics.event('Switch origin', { origin: '', ...context });
      props.setFilter(`${clearFilterOrigin(props.filter)}`);
    }
  }
  function clickOther() {
    if (props.result.origin !== 'other' ) {
      analytics.event('Switch origin', { origin: 'other', ...context });
      props.setFilter(`origin:other ${clearFilterOrigin(props.filter)}`);
    } else {
      analytics.event('Switch origin', { origin: '', ...context });
      props.setFilter(`${clearFilterOrigin(props.filter)}`);
    }
  }

  const origin = props.result?.origin;
  const originOther =  origin === 'other' ;
  const originPlatform =  origin === 'platform';
  return (
    <>
      {props.result?.filtered && (
        <>
          <div className="search-results-info">
            {props.result.all.length === 0 && <b>No extensions found </b>}
            {props.result.all.length > 0 && (
              <span className='origins-count'>
                <span className='results'>Extensions found by origin: </span>
                <span className={classNames('origin-count', 'platform-origin', { 'current-origin': originPlatform })} onClick={clickPlatform}>
                  {originPlatform && <FaCheck />}<span className='count'>{props.result.platform.length}</span> in platform <PlatformIcon />
                </span>
                <span className={classNames('origin-count', 'other-origin', { 'current-origin': origin === 'other' })} onClick={clickOther}>
                  {originOther && <FaCheck />}<span className='count'>{props.result.other.length}</span> in other
                </span>
              </span>
            )}

          </div>
          {props.result.selected.length === 0 && props.result.other.length > 0 && (
            <Alert variant="info" className="search-results-alert">
              No extensions found in platform. <Button as="a" onClick={clickOther}>Showing {props.result.other.length} <Plural count={props.result.other.length} label="extension" /> from other origin</Button>.
            </Alert>
          )}
          {props.result.selected.length === 0 && props.result.platform.length > 0 && (
            <Alert variant="info" className="search-results-alert">
              No extensions found in other origin. <Button as="a" onClick={clickPlatform}>Showing {props.result.platform.length} <Plural count={props.result.platform.length} label="extension" /> from platform</Button>.
            </Alert>
          )}
        </>
      )}
    </>
  )
}
