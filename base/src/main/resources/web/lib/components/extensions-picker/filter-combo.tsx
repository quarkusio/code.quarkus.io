import {useState} from "react";
import {Dropdown} from "react-bootstrap";
import {FaAngleUp, FaAngleDown, FaCheckSquare, FaRegSquare, FaCheckCircle, FaRegCircle, FaTimes, FaStar, FaMinus} from "react-icons/fa";
import {MetadataFilterValues} from "./extensions-utils";
import classNames from 'classnames';

interface FilterComboProps {
  label: string;
  values: MetadataFilterValues;
  onToggleValue: (value: string, active: boolean) => void;
  onSelectAll?: () => void;
  onExclude?: () => void;
  onClearAll?: () => void;
}

export function FilterCombo({
                              label,
                              values,
                              onExclude,
                              onToggleValue,
                              onSelectAll,
                              onClearAll,
                            }: FilterComboProps) {
  const [isOpen, setIsOpen] = useState(false);

  const allValues = values.all;
  const selectIcons = values.radio ?
    [<FaCheckCircle/>, <FaRegCircle/>] : [<FaCheckSquare/>, <FaRegSquare/>]
  return (
    <Dropdown
      className="filter-combo"
      onToggle={setIsOpen}
      show={isOpen}
    >
      <Dropdown.Toggle className="filter-combo-button" as="div" aria-label={`Toggle ${label} combobox`}>
        {label} {isOpen ? <FaAngleUp/> : <FaAngleDown/>}
      </Dropdown.Toggle>

      <Dropdown.Menu align="left">

        <div className="filter-combo-actions">
          {onClearAll && (values.active.length > 0 || values.exclude) &&
              <button onClick={onClearAll} aria-label={`Drop ${label} filter`}><FaTimes/>Drop {label} filter</button>}
          {onSelectAll && values.optional && !values.any &&
              <button onClick={onSelectAll}  aria-label={`Add has ${label} filter`}><FaStar/>Has {label}</button>}
          {onExclude && values.optional && !values.exclude &&
              <button onClick={onExclude}  aria-label={`Add no ${label} filter`}><FaMinus/>No {label}</button>}
        </div>

        {allValues.map((item, idx) => (
          <Dropdown.Item
            as="div"
            key={idx}
            className={classNames('filter-option', item.active ? "active" : "inactive")}
            onClick={() => onToggleValue(item.value, item.active)}
            aria-label={`${item.active ? 'Remove' : 'Add'} ${label}:${item.label} filter`}
          >
            {item.active ? selectIcons[0] : selectIcons[1]}
            <span className='label'>{item.label}</span>
          </Dropdown.Item>
        ))}

      </Dropdown.Menu>
    </Dropdown>
  );
}