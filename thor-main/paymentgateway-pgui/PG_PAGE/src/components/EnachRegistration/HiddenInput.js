const HiddenInput = props => {
    return (
        <input type="hidden" name="merchantReturnUrl" value={props.inputValue} />
    );
}

export default HiddenInput;