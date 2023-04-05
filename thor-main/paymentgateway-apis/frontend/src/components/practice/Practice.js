
// import React, { useState } from 'react'; 
   
// const Example=()=> {
//   const [change, setChange] = useState("");  
//   const handleChange = () => {
//       setChange("hello");
//   }

//   const handleSubmit = () => {
//       console.log(change);
//   }
  
//       return (
//         <div>
//         <button onClick={handleChange}>
//           Click Here! { change }
//         </button>
//         {change == "hi" ?<h1>Welcome to GeeksforGeeks</h1>:
//                 <h1>A Computer Science Portal for Geeks</h1>}
//         <button onClick={handleSubmit}>new</button>
//         </div>
//         );
//   }
  
// export default Example;

import React, { Component } from 'react';  
class Example extends React.Component {  
 constructor() {  
      super();        
      this.state = { displayBio: true };
      }  
      togglerBinder = () => {
          this.setState({
              displayBio : false
          })
      }

      render() {  
          const bio = this.state.displayBio ? (  
              <div>  
                  <p><h3>Javatpoint is one of the best Java training institute in Noida, Delhi, Gurugram, Ghaziabad and Faridabad. We have a team of experienced Java developers and trainers from multinational companies to teach our campus students.</h3></p>   
                  <button onClick={this.togglerBinder}>click button</button>
            </div>  
              ) : null;  
              return (  
                  <div>  
                      <h1> Welcome to JavaTpoint!! </h1>  
                      { bio }   
                  </div>  
              );  
     }  
}  
export default Example;  