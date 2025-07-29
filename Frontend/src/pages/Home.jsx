import Header from "../Components/header.jsx";
import MenuBar from "../Components/MenuBar.jsx";

const Home = () => {
    return (
       <div className="flex flex-col items-center justify-center min-vh-100">
           <MenuBar />
           <Header/>
       </div>
    )
}
export default Home;