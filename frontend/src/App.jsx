import { Navigate, Route, Routes } from "react-router-dom";
import AppShell from "./components/app/AppShell.jsx";
import DiscoverPage from "./pages/DiscoverPage.jsx";
import PublishStubPage from "./pages/PublishStubPage.jsx";

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<Navigate to="/discover" replace />} />
        <Route path="/discover" element={<DiscoverPage />} />
        <Route path="/publish" element={<PublishStubPage />} />
        <Route path="*" element={<Navigate to="/discover" replace />} />
      </Route>
    </Routes>
  );
}
