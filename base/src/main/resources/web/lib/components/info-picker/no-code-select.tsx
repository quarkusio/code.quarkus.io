import * as React from 'react';
import { InputProps } from '../../core/types';
import { useAnalyticsEditionField } from '../../core/analytics';
import { Form, OverlayTrigger, Tooltip } from 'react-bootstrap';


export const NoCodeSelect = (props: InputProps<boolean>) => {
  const onChangeWithDirty = useAnalyticsEditionField('no-code', props.onChange)[1];
  const adaptedOnChange = (e: any) => {
    onChangeWithDirty(!e.currentTarget.checked);
  };
  const tooltip = (
    <Tooltip id="no-code-select" style={{ zIndex: 200 }}>
       Decide whether to include the starter code provided by extensions or get an empty Quarkus project.
    </Tooltip>
  );
  return (
    <OverlayTrigger
      placement="right"
      overlay={tooltip}
      delay={{ show: 200, hide: 0 }}
    >
      <div className="form-group code-switch">
        <Form.Control
          type="checkbox"
          id="code"
          checked={!props.value}
          className="code-switch-control"
          onChange={adaptedOnChange}
        />
        <label className="form-group-label code-switch-label" htmlFor="code" aria-label="Starter Code"><div className="code-switch-inner-label">Starter Code</div></label>
      </div>
    </OverlayTrigger>
  );
};
