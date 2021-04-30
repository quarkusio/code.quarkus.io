import { InputProps, useAnalyticsEditionField } from '../../core';
import React from 'react';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';


export const NoCodeSelect = (props: InputProps<boolean>) => {
  const onChangeWithDirty = useAnalyticsEditionField('no-code', props.onChange)[1];
  const adaptedOnChange = (e: any) => {
    onChangeWithDirty(e.currentTarget.value);
  };
  const Overlay = (
    <Tooltip id="no-code-select" style={{ zIndex: 200 }}>
       Say "Yes" to include the starter code provided by extensions and "No" for an empty Quarkus project.
    </Tooltip>
  );
  return (
    <OverlayTrigger
      placement="right"
      overlay={Overlay}
      delay={{ show: 200, hide: 0 }}
    >
      <div className="form-group">
        <label className="form-group-label" htmlFor="no-code" aria-label="Starter Code"><span><span className="codestart-icon" />Starter Code</span></label>

        <select id="no-code" value={props.value ? 'true' : 'false'} onChange={adaptedOnChange} className={'form-group-control'}>
          <option value={'false'}>Yes</option>
          <option value={'true'}>No</option>
        </select>
      </div>
    </OverlayTrigger>
  );
};
