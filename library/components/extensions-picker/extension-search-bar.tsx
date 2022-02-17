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
    analytics.event('UX', 'Switch origin', 'platform');
    props.setFilter(`origin:platform ${clearFilterOrigin(props.filter)}`);
  }
  function clickOther() {
    analytics.event('UX', 'Switch origin', 'other');
    props.setFilter(`origin:other ${clearFilterOrigin(props.filter)}`);
  }
  function clickAny() {
    analytics.event('UX', 'Switch origin', 'any');
    props.setFilter(`origin:any ${clearFilterOrigin(props.filter)}`);
  }
  const origin = props.result?.origin;
  return (
    <Dropdown  className="filter-shortcut" onClick={(e) => e.stopPropagation()} onToggle={setIsOpen} show={isOpen}>
      <Dropdown.Toggle className="filter-shortcut-button">
        Filters {isOpen ? <FaAngleUp /> : <FaAngleDown />}
      </Dropdown.Toggle>
      <Dropdown.Menu
        align="left"
        onClick={(e) => e.stopPropagation()}
      >
        {props.result?.metadata?.categories?.length > 0 &&
          <DropdownItem as={ListItem}>
              With category&nbsp;
            {props.result?.metadata?.categories?.map((c, i) => (
              <span className="category" key={i} onClick={() => props.setFilter((f) => `cat:${c} ${f}`)}>{c}</span>
            ))}
          </DropdownItem>
        }
        {props.result?.metadata?.tags?.length > 0 &&
          <DropdownItem as={ListItem}>
              With tag&nbsp;
            {props.result?.metadata?.tags?.map((t, i) => (
              <span className="tag" key={i} onClick={() => props.setFilter((f) => `tag:${t} ${f}`)}>{t}</span>
            ))}
          </DropdownItem>
        }
        <DropdownItem as={ListItem}>
          From origin&nbsp;
          <span onClick={clickPlatform} className={classNames( 'origin', { 'current-origin': origin === 'platform' })}>platform</span>
          <span onClick={clickOther}  className={classNames( 'origin', { 'current-origin': origin === 'other' })}>other</span>
          <span onClick={clickAny}  className={classNames( 'origin', { 'current-origin': origin === 'any' })}>any</span>
        </DropdownItem>
        {props.result?.filtered && (
          <DropdownItem key="clear" as={Button} onClick={() => props.setFilter(DEFAULT_FILTER)}>
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
