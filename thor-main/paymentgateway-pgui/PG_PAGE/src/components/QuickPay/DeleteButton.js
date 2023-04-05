const DeleteButton = props => {
    return (
        <button type="button"
            id={"deleteButton" + props.elementKey}
            onClick={e => props.deleteHandler(props.elementKey, props.action, props.confirmText, props.checkBoxId, props.tokenName, props.tokenAvailableName, e)}
            className="close btn-close-desktop">
            <i className="pg-icon icon-bin"></i>
        </button>
    );
}

export default DeleteButton;