import React, { SVGProps } from 'react';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';

// Generated using svgr
export function PlatformIcon(props: SVGProps<SVGSVGElement>){
  return (
    <svg
      className="platform-icon"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 32 32"
      enableBackground="new 0 0 32 32"
      xmlSpace="preserve"
      width="1em"
      height="1em"
      {...props}
    >
      <path
        className="platform-icon_svg__st0"
        d="m16 4.1 10.3 6V22L16 28 5.7 22V10L16 4.1M16 0 2.1 8v16L16 32l13.9-8V8L16 0z"
      />
      <path
        className="platform-icon_svg__st0"
        d="M23.8 11.5c-.5-.8-1.6-1.1-2.4-.7L16 14l-5.4-3.1c-.8-.5-1.9-.2-2.4.6s-.2 1.9.6 2.4l5.4 3.1v6.2c0 1 .8 1.8 1.7 1.8 1 0 1.8-.8 1.8-1.7v-6.2l5.4-3.1c.9-.6 1.2-1.6.7-2.5z"
      />
    </svg>
  );
}

export function ExtensionsOrigin(props: { platform: boolean }) {
  const Overlay = (
    <Tooltip id="extension-origin-platform-tooltip" style={{ zIndex: 200 }}>
      The quark icon indicates the extension is part of the selected Quarkus Platform. Extensions in a platform are tested and verified together and thus safer to use and easier to upgrade.
    </Tooltip>
  );
  return (
    props.platform ? <OverlayTrigger
      placement="bottom"
      overlay={Overlay}
      delay={{ show: 200, hide: 0 }}
    >
      <span className="extension-origin-platform">
        <PlatformIcon />
      </span>
    </OverlayTrigger> : <React.Fragment/>
  )
}
