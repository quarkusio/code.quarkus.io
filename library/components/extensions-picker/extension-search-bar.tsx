import React, { useState } from 'react';
import { Button, Dropdown, Form, FormGroup } from 'react-bootstrap';
import { FaAngleDown, FaAngleUp, FaSearch } from 'react-icons/fa';
import { QuarkusProject } from '../api/model';
import './extension-search-bar.scss';
import DropdownItem from 'react-bootstrap/DropdownItem';
import { clearFilterOrigin, FilterResult } from './extensions-utils';
import classNames from 'classnames';
import { DEFAULT_FILTER } from '../api/quarkus-project-utils';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import * as _ from 'lodash';

export interface ExtensionSearchBarProps {
  placeholder: string;
  filter: string;
  project: QuarkusProject | undefined;
  setFilter: (string) => void;
  result: FilterResult
}

function ListItem(props: { className?: string, children: React.ReactChildren }) {
  const className = `${props.className || ''} list-item`;
  return (
    <div {...props} className={className}>{props.children}</div>
  )
}

function FilterShortcutsDropdown(props: ExtensionSearchBarProps) {
  const [ isOpen , setIsOpen ] = useState(false);
  const analytics = useAnalytics();
  function clickPlatform() {
    if (props.result.origin !== 'platform') {
      analytics.event('UX', 'Switch origin', 'platform');
      props.setFilter(`origin:platform ${clearFilterOrigin(props.filter)}`);
    } else {
      analytics.event('UX', 'Switch origin', 'clear');
      props.setFilter(`${clearFilterOrigin(props.filter)}`);
    }
  }
  function clickOther() {
    if (props.result.origin !== 'other' ) {
      analytics.event('UX', 'Switch origin', 'other');
      props.setFilter(`origin:other ${clearFilterOrigin(props.filter)}`);
    } else {
      analytics.event('UX', 'Switch origin', 'clear');
      props.setFilter(`${clearFilterOrigin(props.filter)}`);
    }
  }

  function addFilter(k: string, v:string) {
    props.setFilter(f => `${k}:${v} ${f}`);
  }

  function clearFilter(k: string, v:string) {
    props.setFilter(f => f.replaceAll(new RegExp(`\\s*${k}:${v}\\s*`, 'ig'), ''));
  }
  const origin = props.result?.origin;
  const filters = _.sortBy(Object.entries(props.result?.filters || {}), f => f[0]);
  return (
    <Dropdown  className="filter-shortcut" onClick={(e) => e.stopPropagation()} onToggle={setIsOpen} show={isOpen}>
      <Dropdown.Toggle className="filter-shortcut-button">
        Filters {isOpen ? <FaAngleUp /> : <FaAngleDown />}
      </Dropdown.Toggle>
      <Dropdown.Menu
        align="left"
        onClick={(e) => e.stopPropagation()}
      >
        <DropdownItem as={ListItem}>
          origin:&nbsp;
          <span onClick={clickPlatform} className={classNames( 'origin', { 'active': origin === 'platform' })}>platform</span>
          <span onClick={clickOther}  className={classNames( 'origin', { 'active': origin === 'other' })}>other</span>
        </DropdownItem>
        {filters.length > 0 && filters.map((t: any, i) => (
          <DropdownItem as={ListItem} key={i}>
            {t[0]}:&nbsp;
            {t[1].active.map((v, i) => (
              <span className={`${t[0]} active`} key={i} onClick={() => clearFilter(t[0], v)}>{v}</span>
            ))}
            {t[1].inactive.map((v, i) => (
              <span className={`${t[0]} inactive`} key={i} onClick={() => addFilter(t[0], v)}>{v}</span>
            ))}
          </DropdownItem>
        ))}
        {props.result?.filtered && (
          <DropdownItem key="clear" className='clear-filter' as={Button} onClick={() => props.setFilter(DEFAULT_FILTER)}>
          Clear filters
          </DropdownItem>
        )}
      </Dropdown.Menu>
    </Dropdown>
  )
}


export function ExtensionSearchBar(props: ExtensionSearchBarProps) {
  const { filter, setFilter } = props;
  const search = (e: any) => {
    setFilter(e.currentTarget.value);
  };

  return (
    <div className="search-bar responsive-container">
      <FilterShortcutsDropdown {...props} />
      <FormGroup
        controlId="extensions-search-input"
      >
        <FaSearch className="search-icon" />
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
