import "./input.css";

const FormInput = (label) => {
    return (
        <div className="zk-form_group">
            {/* { label && <label htmlFor="">Search</label> } */}
            <input type="text" placeholder="Search" className="zk-form_control" />
        </div>
    )
}

export default FormInput;