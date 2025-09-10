import React from 'react';
import {Button, Form, FormGroup} from 'react-bootstrap';
import {FaSearch, FaTimes, FaCaretUp, FaCaretDown} from 'react-icons/fa';
import {QuarkusProject} from '../api/model';
import './extension-search-bar.scss';
import {
  addMetadataFilter,
  addStarMetadataFilter, addExcludeMetadataFilter,
  clearMetadataFilter,
  FilterResult,
  removeMetadataFilter
} from './extensions-utils';
import {DEFAULT_FILTER, isFilterEmpty} from '../api/quarkus-project-utils';
import {useAnalytics} from '../../core/analytics';
import * as _ from 'lodash';
import {FilterCombo} from "./filter-combo";

export interface ExtensionSearchBarProps {
  placeholder: string;
  filter: string;
  project: QuarkusProject | undefined;
  setFilter: React.Dispatch<React.SetStateAction<string>>;
  showList: boolean;
  toggleShowList: () => void;
  result: FilterResult
}

function FiltersBar(props: ExtensionSearchBarProps) {
  const analytics = useAnalytics();
  const context = {element: 'extension-search-dropdown'};

  const filters = _.sortBy(Object.entries(props.result?.filters || {}), f => f[0]);

  function clickPlatform() {
    analytics.event('Switch origin', {origin: 'platform', ...context});
  }

  return (
    <div className="filters-bar">
      {props.result?.filtered ? (
        <div className="search-results-info">
          {props.result.entries.length === 0 && <b>No extensions found </b>}
          {props.result.entries.length > 0 && (
            <span className='result-count'>
                <span className='count'>{props.result.entries.length}</span>
                <span className='info'>extensions found</span>
              </span>
          )}

        </div>
      ) : (props.showList ? (
        <Button className='button-toggle-list' aria-label="Toggle full list of extensions"
                onClick={props.toggleShowList}><FaCaretUp/>Hide the full list</Button>) : (
        <div className="toggle-list"><Button className='button-toggle-list main-title'
                                             aria-label="Toggle full list of extensions"
                                             onClick={props.toggleShowList}><FaCaretDown/>Toggle the full list of
          extensions</Button></div>
      ))
      }
      <div className="filters">
        <h3>Filter By</h3>
        {filters.map(([key, value]: any, i) => (
          <FilterCombo
            key={i}
            label={key}
            values={value}
            onToggleValue={(v, isActive) => {
              if (isActive) {
                props.setFilter(f => removeMetadataFilter(props.result.filters, f, key, v));
              } else {
                props.setFilter(f => addMetadataFilter(props.result.filters, f, key, v));
              }
            }}
            onSelectAll={() => {
              props.setFilter(f => addStarMetadataFilter(f, key));
            }}
            onClearAll={() => {
              props.setFilter(f => clearMetadataFilter(f, key));
            }}
            onExclude={() => {
              props.setFilter(f => addExcludeMetadataFilter(f, key));
            }}
          />
        ))}
      </div>
    </div>
  );
}

export function ExtensionSearchBar(props: ExtensionSearchBarProps) {
  const {filter, setFilter} = props;
  const search = (e: any) => {
    setFilter(e.currentTarget.value);
  };

  function clearFilters() {
    setFilter(DEFAULT_FILTER);
  }

  return (
    <div className="search-bar responsive-container">

      <FormGroup
        controlId="extensions-search-input"
      >
        <div className="search-icon"><FaSearch/></div>
        <Form.Control
          type="search"
          aria-label="Search extensions"
          placeholder={props.placeholder}
          className="extensions-search-input"
          autoComplete="off"
          value={filter}
          onChange={search}
        />
        {!isFilterEmpty(filter) &&
            <Button as="a" className='clear-button' onClick={clearFilters} aria-label="Clear filters"><FaTimes/><span>Clear filters</span></Button>
        }

      </FormGroup>
      <FiltersBar {...props} />
    </div>
  );
}
