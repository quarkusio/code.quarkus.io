import React, { SetStateAction } from 'react';
import { Button, Form, FormGroup } from 'react-bootstrap';
import { FaCheck, FaSearch } from 'react-icons/fa';
import { QuarkusProject } from '../api/model';
import './extension-search-bar.scss';
import { clearFilterOrigin, FilterResult } from './extensions-utils';
import classNames from 'classnames';

export interface ExtensionSearchBarProps {
  placeholder: string;
  filter: string;
  project: QuarkusProject | undefined;
  setFilter: (string) => void;
  setKeyBoardActivated: (number) => void;
}

export function ExtensionSearchBar(props: ExtensionSearchBarProps) {
  const { filter, setFilter, setKeyBoardActivated } = props;
  const search = (e: any) => {
    setKeyBoardActivated(-1);
    setFilter(e.currentTarget.value);
  };

  return (
    <div className="search-bar responsive-container">
      <FormGroup
        controlId="extensions-search-input"
      >
        <FaSearch/>
        <Form.Control
          type="search"
          aria-label="Search extensions"
          placeholder={props.placeholder}
          className="extensions-search-input"
          autoComplete="off"
          value={filter}
          onChange={search}
        />
      </FormGroup>
    </div>
  );
}

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
      {props.result && (
        <div className="search-results-info">
          {props.result.any.length === 0 && <b>No extension found </b>}
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
