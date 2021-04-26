import { InputProps, useAnalyticsEditionField } from '../../core';
import React, { ChangeEvent } from 'react';
import { FormGroup, Tooltip } from '@patternfly/react-core';


export const NoCodeSelect = (props: InputProps<boolean>) => {
    const onChangeWithDirty = useAnalyticsEditionField('no-examples', props.onChange)[1];
    const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
        onChangeWithDirty(e.target.value === 'true', e);
    };
    return (
        <Tooltip
            position="right"
            content={<span>The flag <span className="codestart-example-icon" /> means the extension helps you get started with example code. You can choose to include all the examples or have an empty project....</span>}
            exitDelay={0}
            zIndex={200}
        >
            <FormGroup
                fieldId="no-examples"
                label={<span><span className="codestart-example-icon" />Example code</span>}
                aria-label="Examples">
                <select id="no-examples" value={props.value ? 'true' : 'false'} onChange={adaptedOnChange} className={'pf-c-form-control'}>
                    <option value={'false'}>Yes, Please</option>
                    <option value={'true'}>No, Thanks</option>
                </select>
            </FormGroup>
        </Tooltip>
    );
};
