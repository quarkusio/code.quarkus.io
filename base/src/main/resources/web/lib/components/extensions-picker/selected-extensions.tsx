import React from 'react';
import {ExtensionRow} from "./extension-row";
import styled from 'styled-components';
import {FaExclamation, FaTrashAlt} from 'react-icons/fa';
import {Alert} from 'react-bootstrap';
import {ExtensionEntry, TagEntry} from "./extensions-picker";
import classNames from 'classnames';


const SelectedExtensionsDiv = styled.div`
    button.btn.btn-clear {
        color: var(--warningColor);
        font-weight: bold;
        float: right;

        svg {
            color: var(--warningColor);
        }


    }

    &.picker {
        h4 {
            border-bottom: 1px solid var(--extensionsPickerCategoryUnderlineColor);
        }
    }

    &.cart {
        display: flex;
        flex-direction: column;

        .alert-warning {
            background-color: transparent;
            color: var(--dropdownMenuTextColor);
            border: none;
            padding: 0;
        }

        .extension-row {
            flex-wrap: nowrap;
            height: 22px;
            line-height: 22px;

            .extension-name {
                line-height: 22px;
                height: 22px;
            }

            .extension-origin-platform {
                margin-left: 5px;
            }

            .extension-id {
                display: none;
            }

            .extension-tag {
                margin: 0 0 0 5px;
            }
        }

        .extension-summary:before {
            content: '-';
            margin-right: 5px;
            margin-left: 5px;
        }

        .extension-summary {
            flex-grow: 1;
            text-overflow: ellipsis;
        }

        .extension-remove {
            order: 3;
        }
    }

`


export const SelectedExtensions = (props: {layout?: 'cart' | 'picker'; extensions: ExtensionEntry[]; tagsDef: TagEntry[]; remove: (id: string, type: string) => void }) => {
  const clear = () => {
    props.remove('*', 'Selection clear');
  };
  const layout = props.layout || 'cart';
  return (
    <SelectedExtensionsDiv className={classNames('selected-extensions', layout)}>
      <h4>
        Selected Extensions
        {props.extensions.length > 0 && <button className="btn btn-light btn-clear" onClick={clear} aria-label="Clear extension selection">
            <FaTrashAlt/>Clear selection
        </button>}
      </h4>
      {props.extensions.length === 0 && (
        <Alert variant="warning">
          <FaExclamation/>&nbsp;You haven't selected any extension for your Quarkus application. Browse and select from
          the list below.
        </Alert>
      )}
      {props.extensions.length > 0 && (
        <>
          <div className="extension-list-wrapper">
            {
              props.extensions.map((ex, i) => (
                <ExtensionRow
                  {...ex}
                  key={i}
                  selected={true}
                  onClick={() => props.remove(ex.id, 'Selection')}
                  layout={layout}
                  tagsDef={props.tagsDef}
                />
              ))
            }
          </div>

        </>
      )}
    </SelectedExtensionsDiv>
  );
}

