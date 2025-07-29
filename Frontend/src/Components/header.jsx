import { assets } from "../assets/assets";

const Header=()=>
{
    return(
        <div className="text-center d-flex flex-column align-items-center justify-content-center py-5 px-3">
           <img src={assets.AuthLogo} alt="AuthLogo" width={120} className="mb-4" />
         <h5 className="fw-semibold"> 
            Hey Developer <span role="img" aria-label="wave">👋</span>
         </h5 >
         <h1 className="fw-bold display-5 mb-3">Welcome to the Authenticator</h1>
         <p className="text-muted fs-5 mb-4">
            lets's start with a quick product tour and then you can start using the app.
         </p>
         <button className="btn btn-outline-dark rounded-pill px-4 py-2">
            Get Started
         </button>
        </div>
    )
}

export default Header;