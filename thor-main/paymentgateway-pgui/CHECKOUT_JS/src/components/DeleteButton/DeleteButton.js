const DeleteButton = props => {
    return (
        <button type="button"
            id={"deleteButton" + props.elementKey}
            onClick={e => props.deleteHandler(props.elementKey, props.action, props.confirmText, props.checkBoxId, props.tokenName, props.tokenAvailableName, e)}
            className="close btn-close-mobile border-none bg-none position-absolute right-0 top-0 h-100 font-size-13 px-15">
            <i className="pg-icon icon-bin"></i>
        </button>
    );
}

export default DeleteButton;