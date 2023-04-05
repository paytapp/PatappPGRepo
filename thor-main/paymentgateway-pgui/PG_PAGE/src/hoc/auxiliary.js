import useWindowSize from "../utils/useWindowSize";

const auxillary = Component => {
    return (props) => {
        const { width } = useWindowSize();

        return <Component width={width} {...props} />;
    }
};

export default auxillary;