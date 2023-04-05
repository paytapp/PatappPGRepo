import React, { Component } from "react";
import { multilingual } from "../../js/script";

class Language extends Component {
    render() {
        return (
            <div id="lang-switch-desktop">
                <select id="translate" className="form-control max-width-200" onChange={multilingual}>
                    <option value="english">English</option>
                    <option value="hindi">Hindi</option>
                    <option value="punjabi">Punjabi</option>
                    <option value="urdu">Urdu</option>
                    <option value="arabic">Arabic</option>
                    <option value="telugu">Telugu</option>
                    <option value="tamil">Tamil</option>
                    <option value="french">French</option>
                    <option value="spanish">Spanish</option>
                    <option value="malayalam">Malayalam</option>
                    <option value="kannada">Kannada</option>
                    <option value="marathi">Marathi</option>
                </select>
            </div>
        );
    }
}

export default Language;