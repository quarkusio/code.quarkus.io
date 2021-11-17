import React from 'react';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import { FaAtom } from 'react-icons/fa';

export function ExtensionsOrigin(props: { platform: boolean }) {
  const Overlay = (
    <Tooltip id="extension-origin-platform-tooltip" style={{ zIndex: 200 }}>
      The atom indicates the extension is part of the selected Quarkus Platform. Using extensions from the platform are tested and verified together and thus what is default recommended as easier to use and upgrade.
    </Tooltip>
  );
  return (
    props.platform ? <OverlayTrigger
      placement="bottom"
      overlay={Overlay}
      delay={{ show: 200, hide: 0 }}
    >
      <span className="extension-origin-platform">
        <FaAtom />
      </span>
    </OverlayTrigger> : <React.Fragment/>
  )
}