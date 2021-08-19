import React  from 'react';
import { Form, FormGroup } from 'react-bootstrap';
import { FaSearch } from 'react-icons/fa';
import { QuarkusProject } from '../api/model';
import './extension-search-bar.scss';

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
