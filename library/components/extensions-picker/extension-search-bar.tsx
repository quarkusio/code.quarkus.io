import React, { useState } from 'react';
import { Button, Dropdown, Form, FormGroup } from 'react-bootstrap';
import { FaAngleDown, FaAngleUp, FaSearch } from 'react-icons/fa';
import { QuarkusProject } from '../api/model';
import './extension-search-bar.scss';
import DropdownItem from 'react-bootstrap/DropdownItem';
import { FilterResult } from './extensions-utils';

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
  return (
    <Dropdown  className="filter-shortcut" onClick={(e) => e.stopPropagation()} onToggle={setIsOpen} show={isOpen}>
      <Dropdown.Toggle className="filter-shortcut-button">
        Filters {isOpen ? <FaAngleUp /> : <FaAngleDown />}
      </Dropdown.Toggle>
      <Dropdown.Menu
        align="left"
        onClick={(e) => e.stopPropagation()}
      >
        <DropdownItem as={ListItem} >
            with category&nbsp;
          {props.result?.metadata?.categories?.map((c, i) => (
            <span className="category" key={i} onClick={() => props.setFilter((f) => `cat:${c} ${f}`)}>{c}</span>
          ))}
        </DropdownItem>
        <DropdownItem as={ListItem}>
          with tag&nbsp;
          {props.result?.metadata?.tags?.map((t, i) => (
            <span className="tag" key={i} onClick={() => props.setFilter((f) => `tag:${t} ${f}`)}>{t}</span>
          ))}
        </DropdownItem>
        <DropdownItem key="origin-any" as={Button} onClick={() => props.setFilter((f) => `origin:any ${f}`)}>
          From&nbsp;<b>all</b>&nbsp;origins
        </DropdownItem>
        {props.result?.filtered && (
          <DropdownItem key="clear" as={Button} onClick={() => props.setFilter('')}>
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
