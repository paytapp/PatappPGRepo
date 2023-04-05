const NewWindow = {
    iframe: () => {
        let w = 900,
            h = 700,
            dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : window.screenX,
            dualScreenTop = window.screenTop !== undefined ? window.screenTop : window.screenY,
            tempWidth = window.screen.width,
            tempHeight = window.screen.height,
            systemZoom = tempWidth / window.screen.availWidth,
            winLeft = (tempWidth - w) / 2 / systemZoom + dualScreenLeft,
            tempTop = ((tempHeight - h) / 2 / systemZoom + dualScreenTop) - 50,
            winTop = tempTop < 0 ? 0 : tempTop,
            winWidth = w / systemZoom,
            winHeight = h / systemZoom;

        return {
            width: winWidth,
            height: winHeight,
            top: winTop,
            left: winLeft
        }
    }
}

export default NewWindow;