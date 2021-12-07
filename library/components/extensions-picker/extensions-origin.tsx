import React from 'react';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import { CgShapeHexagon } from 'react-icons/cg';

export function ExtensionsOrigin(props: { platform: boolean }) {
  const Overlay = (
    <Tooltip id="extension-origin-platform-tooltip" style={{ zIndex: 200 }}>
      The quark indicates the extension is part of the selected Quarkus Platform. Extensions from the platform are recommended over other extensions: they are tested and verified together and thus easier to use and upgrade.
    </Tooltip>
  );
  return (
    props.platform ? <OverlayTrigger
      placement="bottom"
      overlay={Overlay}
      delay={{ show: 200, hide: 0 }}
    >
      <span className="extension-origin-platform">
        <CgShapeHexagon />
      </span>
    </OverlayTrigger> : <React.Fragment/>
  )
}
