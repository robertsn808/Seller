import { useState, useEffect, createContext, useContext } from "react";

interface Business {
  id: string;
  name: string;
  type: string;
  icon: string;
  location: string;
  description: string;
  context: string;
  menuItems: string[];
  targetAudience: string[];
  specialties: string[];
}

const businesses: Record<string, Business> = {
  "allii-fish-market": {
    id: "allii-fish-market",
    name: "Allii Fish Market",
    type: "Hawaiian Poke Shop",
    icon: "fish",
    location: "Honolulu, Hawaii",
    description: "Premium poke bowls, smoked meats, and authentic Hawaiian cuisine",
    context: `Allii Fish Market is a premium Hawaiian poke shop in Honolulu specializing in:
- Fresh poke bowls (Limu Ahi, Spicy Creamy Garlic Ahi, Wasabi Ginger A'u)
- Smoked meats (Pipikaula beef jerky, Lechon pork)
- Specialty items (Inari Poke Bombs, Wasabi Fried Chicken)
- Authentic Hawaiian flavors with modern presentation
- Located in Honolulu, serving locals and tourists
- Focus on fresh, daily-sourced fish and traditional preparation methods`,
    menuItems: [
      "Limu Ahi Poke Bowl",
      "Spicy Creamy Garlic Ahi",
      "Wasabi Ginger A'u",
      "Pipikaula Beef Jerky",
      "Lechon Pork",
      "Inari Poke Bombs",
      "Wasabi Fried Chicken"
    ],
    targetAudience: ["locals", "tourists", "poke enthusiasts", "seafood lovers", "Hawaiian culture enthusiasts"],
    specialties: ["fresh fish", "Hawaiian authenticity", "traditional recipes", "daily sourcing"]
  },
  "allii-coconut-water": {
    id: "allii-coconut-water",
    name: "Allii Coconut Water",
    type: "Beverage Company",
    icon: "coconut",
    location: "Hawaii & Mainland",
    description: "Organic coconut water and tropical beverages",
    context: `Allii Coconut Water is a premium beverage company specializing in:
- 100% organic coconut water sourced from Hawaiian coconuts
- Natural electrolyte-rich hydration drinks
- Tropical fruit-infused coconut waters (Pineapple, Mango, Passion Fruit)
- Sustainable farming practices and eco-friendly packaging
- Direct-from-source freshness with no added sugars or preservatives
- Distributed across Hawaii and expanding to mainland US markets
- Focus on health-conscious consumers and active lifestyle enthusiasts`,
    menuItems: [
      "Pure Organic Coconut Water",
      "Pineapple Coconut Fusion",
      "Mango Coconut Blend",
      "Passion Fruit Coconut",
      "Coconut Water with Electrolytes",
      "Coconut Water + Vitamin C",
      "Sparkling Coconut Water"
    ],
    targetAudience: ["health-conscious consumers", "athletes", "yoga practitioners", "tropical drink lovers", "organic lifestyle enthusiasts"],
    specialties: ["organic sourcing", "no artificial additives", "sustainable practices", "electrolyte balance", "Hawaiian coconuts"]
  }
};

interface BusinessContextType {
  currentBusiness: Business;
  switchBusiness: (businessId: string) => void;
  getBusinessContext: () => string;
  getTargetAudiences: () => string[];
  getMenuItems: () => string[];
  getSpecialties: () => string[];
}

const BusinessContext = createContext<BusinessContextType | undefined>(undefined);

export function BusinessProvider({ children }: { children: React.ReactNode }) {
  const [currentBusinessId, setCurrentBusinessId] = useState<string>("allii-fish-market");

  useEffect(() => {
    // Load from localStorage if available
    const saved = localStorage.getItem('selectedBusiness');
    if (saved && businesses[saved]) {
      setCurrentBusinessId(saved);
    }

    // Listen for business changes from the switcher
    const handleBusinessChange = (event: CustomEvent) => {
      setCurrentBusinessId(event.detail.id);
    };

    window.addEventListener('businessChanged', handleBusinessChange as EventListener);
    return () => {
      window.removeEventListener('businessChanged', handleBusinessChange as EventListener);
    };
  }, []);

  const switchBusiness = (businessId: string) => {
    if (businesses[businessId]) {
      setCurrentBusinessId(businessId);
      localStorage.setItem('selectedBusiness', businessId);
    }
  };

  const currentBusiness = businesses[currentBusinessId];

  const contextValue: BusinessContextType = {
    currentBusiness,
    switchBusiness,
    getBusinessContext: () => currentBusiness.context,
    getTargetAudiences: () => currentBusiness.targetAudience,
    getMenuItems: () => currentBusiness.menuItems,
    getSpecialties: () => currentBusiness.specialties,
  };

  return (
    <BusinessContext.Provider value={contextValue}>
      {children}
    </BusinessContext.Provider>
  );
}

export function useBusinessContext() {
  const context = useContext(BusinessContext);
  if (context === undefined) {
    throw new Error('useBusinessContext must be used within a BusinessProvider');
  }
  return context;
}

export { businesses };