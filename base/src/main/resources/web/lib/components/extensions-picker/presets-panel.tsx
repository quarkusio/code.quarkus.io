import styled from 'styled-components';
import {TagEntry} from "./extensions-picker";
import {Platform, Preset} from "../api/model";
import _ from "lodash";

interface PresetsProps {
  platform: Platform,
  select?: (id: string, type: string) => void
}


const PresetsPanelDiv = styled.div`


    .panel-title {
        font-weight: bold;
        color: var(--extensionsPickerCategoryTextColor);
        font-size: 1.1rem;
        margin: 10px 0 10px 0;
        height: 30px;
    }

    .presets-list {
        display: flex;
        flex-wrap: wrap;
        justify-items: left;
        gap: 10px;
    }

    .preset-card {
        flex-basis: 292px;
        border: 1px solid var(--presetsCardBorderColor);
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        cursor: pointer;
    }

    .preset-title {
        color: var(--presetsCardTextColor);
        font-size: 1.2rem;
        text-align: center;
    }

    .preset-icon {
        flex-basis: 130px;
    }

    .preset-icon img {
        width: 100px;
    }
    
`;

export const PresetCard = (props: { preset: Preset, tagsDef: TagEntry[], onClick?: () => void }) => {
  return (
    <div className="preset-card" onClick={props.onClick} aria-label={`Select ${props.preset.key} preset`}>
      <div className="preset-icon"><img src={props.preset.icon}/></div>
      <div className="preset-title">{props.preset.title}</div>
    </div>
  );
}

export const PresetsPanel = (props: PresetsProps) => {
  const byId = _.keyBy(props.platform.extensions, ({id}) => id);
  const presets = props.platform.presets.map(p => ({
    ...p, resolvedExtensions: p.extensions.filter(e => byId[e]).map(e => byId[e])
  } as Preset))

  const selectPreset = (preset: Preset) => {
    preset.extensions.forEach(e => props.select(e, "presets"));
  };
  return (
    <PresetsPanelDiv className="presets-panel">
      <div className="panel-title">Start from an extensions presets</div>
      <div className="presets-list">
        {presets.map(p => (
          <PresetCard key={p.key} preset={p} tagsDef={props.platform.tagsDef} onClick={() => selectPreset(p)}/>
        ))}
      </div>
    </PresetsPanelDiv>
  );
}