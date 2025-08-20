import type { Express } from "express";
import { createServer, type Server } from "http";
import OpenAI from "openai";
import { storage } from "./storage";
import { insertCustomerSchema, insertMenuItemSchema, insertInventorySchema, insertOrderSchema, insertGeneratedContentSchema, insertAiInsightSchema, insertBusinessSchema } from "@shared/schema";

// Initialize Grok AI client only if API key is available
const openai = process.env.XAI_API_KEY ? new OpenAI({ 
  baseURL: "https://api.x.ai/v1", 
  apiKey: process.env.XAI_API_KEY 
}) : null;

export async function registerRoutes(app: Express): Promise<Server> {
  // Business endpoints
  app.get("/api/businesses", async (req, res) => {
    try {
      const businesses = await storage.getBusinesses();
      res.json(businesses);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching businesses: " + error.message });
    }
  });

  app.get("/api/businesses/:id", async (req, res) => {
    try {
      const business = await storage.getBusiness(req.params.id);
      if (!business) {
        return res.status(404).json({ message: "Business not found" });
      }
      res.json(business);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching business: " + error.message });
    }
  });

  // Customer endpoints with business context
  app.get("/api/:businessId/customers", async (req, res) => {
    try {
      const customers = await storage.getCustomers(req.params.businessId);
      res.json(customers);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching customers: " + error.message });
    }
  });

  app.get("/api/:businessId/customers/:id", async (req, res) => {
    try {
      const customer = await storage.getCustomer(req.params.businessId, parseInt(req.params.id));
      if (!customer) {
        return res.status(404).json({ message: "Customer not found" });
      }
      res.json(customer);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching customer: " + error.message });
    }
  });

  app.post("/api/:businessId/customers", async (req, res) => {
    try {
      const customerData = insertCustomerSchema.parse({
        ...req.body,
        businessId: req.params.businessId
      });
      const customer = await storage.createCustomer(customerData);
      res.status(201).json(customer);
    } catch (error: any) {
      res.status(400).json({ message: "Error creating customer: " + error.message });
    }
  });

  app.patch("/api/:businessId/customers/:id", async (req, res) => {
    try {
      const customer = await storage.updateCustomer(req.params.businessId, parseInt(req.params.id), req.body);
      res.json(customer);
    } catch (error: any) {
      res.status(400).json({ message: "Error updating customer: " + error.message });
    }
  });

  app.delete("/api/:businessId/customers/:id", async (req, res) => {
    try {
      await storage.deleteCustomer(req.params.businessId, parseInt(req.params.id));
      res.status(204).send();
    } catch (error: any) {
      res.status(400).json({ message: "Error deleting customer: " + error.message });
    }
  });

  // Menu endpoints with business context
  app.get("/api/:businessId/menu", async (req, res) => {
    try {
      const menuItems = await storage.getMenuItems(req.params.businessId);
      res.json(menuItems);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching menu items: " + error.message });
    }
  });

  app.get("/api/:businessId/menu/:id", async (req, res) => {
    try {
      const menuItem = await storage.getMenuItem(req.params.businessId, parseInt(req.params.id));
      if (!menuItem) {
        return res.status(404).json({ message: "Menu item not found" });
      }
      res.json(menuItem);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching menu item: " + error.message });
    }
  });

  app.post("/api/:businessId/menu", async (req, res) => {
    try {
      const menuItemData = insertMenuItemSchema.parse({
        ...req.body,
        businessId: req.params.businessId
      });
      const menuItem = await storage.createMenuItem(menuItemData);
      res.status(201).json(menuItem);
    } catch (error: any) {
      res.status(400).json({ message: "Error creating menu item: " + error.message });
    }
  });

  app.patch("/api/:businessId/menu/:id", async (req, res) => {
    try {
      const menuItem = await storage.updateMenuItem(req.params.businessId, parseInt(req.params.id), req.body);
      res.json(menuItem);
    } catch (error: any) {
      res.status(400).json({ message: "Error updating menu item: " + error.message });
    }
  });

  app.delete("/api/:businessId/menu/:id", async (req, res) => {
    try {
      await storage.deleteMenuItem(req.params.businessId, parseInt(req.params.id));
      res.status(204).send();
    } catch (error: any) {
      res.status(400).json({ message: "Error deleting menu item: " + error.message });
    }
  });

  // Inventory endpoints with business context
  app.get("/api/:businessId/inventory", async (req, res) => {
    try {
      const inventoryItems = await storage.getInventoryItems(req.params.businessId);
      res.json(inventoryItems);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching inventory: " + error.message });
    }
  });

  app.get("/api/:businessId/inventory/:id", async (req, res) => {
    try {
      const inventoryItem = await storage.getInventoryItem(req.params.businessId, parseInt(req.params.id));
      if (!inventoryItem) {
        return res.status(404).json({ message: "Inventory item not found" });
      }
      res.json(inventoryItem);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching inventory item: " + error.message });
    }
  });

  app.post("/api/:businessId/inventory", async (req, res) => {
    try {
      const inventoryData = insertInventorySchema.parse({
        ...req.body,
        businessId: req.params.businessId
      });
      const inventoryItem = await storage.createInventoryItem(inventoryData);
      res.status(201).json(inventoryItem);
    } catch (error: any) {
      res.status(400).json({ message: "Error creating inventory item: " + error.message });
    }
  });

  app.patch("/api/:businessId/inventory/:id", async (req, res) => {
    try {
      const inventoryItem = await storage.updateInventoryItem(req.params.businessId, parseInt(req.params.id), req.body);
      res.json(inventoryItem);
    } catch (error: any) {
      res.status(400).json({ message: "Error updating inventory item: " + error.message });
    }
  });

  app.delete("/api/:businessId/inventory/:id", async (req, res) => {
    try {
      await storage.deleteInventoryItem(req.params.businessId, parseInt(req.params.id));
      res.status(204).send();
    } catch (error: any) {
      res.status(400).json({ message: "Error deleting inventory item: " + error.message });
    }
  });

  // Order endpoints with business context
  app.get("/api/:businessId/orders", async (req, res) => {
    try {
      const orders = await storage.getOrders(req.params.businessId);
      res.json(orders);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching orders: " + error.message });
    }
  });

  app.get("/api/:businessId/orders/:id", async (req, res) => {
    try {
      const order = await storage.getOrder(req.params.businessId, parseInt(req.params.id));
      if (!order) {
        return res.status(404).json({ message: "Order not found" });
      }
      res.json(order);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching order: " + error.message });
    }
  });

  app.post("/api/:businessId/orders", async (req, res) => {
    try {
      const orderData = insertOrderSchema.parse({
        ...req.body,
        businessId: req.params.businessId
      });
      const order = await storage.createOrder(orderData);
      res.status(201).json(order);
    } catch (error: any) {
      res.status(400).json({ message: "Error creating order: " + error.message });
    }
  });

  app.patch("/api/:businessId/orders/:id", async (req, res) => {
    try {
      const order = await storage.updateOrder(req.params.businessId, parseInt(req.params.id), req.body);
      res.json(order);
    } catch (error: any) {
      res.status(400).json({ message: "Error updating order: " + error.message });
    }
  });

  app.delete("/api/:businessId/orders/:id", async (req, res) => {
    try {
      await storage.deleteOrder(req.params.businessId, parseInt(req.params.id));
      res.status(204).send();
    } catch (error: any) {
      res.status(400).json({ message: "Error deleting order: " + error.message });
    }
  });

  // Content generation endpoints with business context
  app.get("/api/:businessId/content", async (req, res) => {
    try {
      const content = await storage.getGeneratedContent(req.params.businessId);
      res.json(content);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching content: " + error.message });
    }
  });

  app.post("/api/:businessId/content/generate", async (req, res) => {
    try {
      const { type, platform, tone, targetAudience, prompt } = req.body;
      const businessId = req.params.businessId;
      
      // Get business context
      const business = await storage.getBusiness(businessId);
      if (!business) {
        return res.status(404).json({ message: "Business not found" });
      }

      // Create business-specific prompt
      const businessContext = businessId === 'allii-fish-market' 
        ? "Allii Fish Market - an authentic Hawaiian poke shop serving fresh, sustainable fish with traditional Hawaiian preparations. Focus on authenticity, freshness, and Hawaiian culture."
        : "Allii Coconut Water - premium organic coconut water sourced from Hawaiian coconuts. Focus on health benefits, purity, and tropical Hawaiian lifestyle.";

      const fullPrompt = `Create ${type} content for ${platform} with a ${tone} tone targeting ${targetAudience}. 

Business Context: ${businessContext}

Content Request: ${prompt}

Please create engaging, authentic content that reflects the brand's Hawaiian heritage and values.`;

      // Generate content using Grok AI
      if (!openai) {
        throw new Error("XAI_API_KEY not configured");
      }
      
      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a professional marketing content creator specializing in Hawaiian businesses. Create authentic, engaging content that reflects local culture and values."
          },
          {
            role: "user",
            content: fullPrompt
          }
        ],
        max_tokens: 1000,
      });

      const generatedContent = response.choices[0].message.content;

      // Save to database
      const contentData = insertGeneratedContentSchema.parse({
        businessId,
        title: `${type} for ${platform}`,
        content: generatedContent,
        type,
        platform,
        tone,
        targetAudience,
        callToAction: "Visit us today!",
        status: "draft"
      });

      const savedContent = await storage.createGeneratedContent(contentData);
      res.status(201).json(savedContent);

    } catch (error: any) {
      console.error("Content generation error:", error);
      res.status(500).json({ message: "Error generating content: " + error.message });
    }
  });

  // AI Insights endpoints with business context
  app.get("/api/:businessId/insights", async (req, res) => {
    try {
      const insights = await storage.getAiInsights(req.params.businessId);
      res.json(insights);
    } catch (error: any) {
      res.status(500).json({ message: "Error fetching insights: " + error.message });
    }
  });

  app.post("/api/:businessId/insights/analyze", async (req, res) => {
    try {
      const { type, data } = req.body;
      const businessId = req.params.businessId;
      
      // Get business context
      const business = await storage.getBusiness(businessId);
      if (!business) {
        return res.status(404).json({ message: "Business not found" });
      }

      const businessContext = businessId === 'allii-fish-market' 
        ? "Hawaiian poke restaurant focusing on fresh fish, traditional preparations, and local ingredients"
        : "Premium coconut water beverage company emphasizing health, purity, and Hawaiian lifestyle";

      let analysisPrompt = "";
      
      switch (type) {
        case "trend-analysis":
          analysisPrompt = `Analyze current food/beverage trends for ${businessContext}. Provide 3-5 actionable insights about menu items, pricing, or marketing opportunities.`;
          break;
        case "competitor-analysis":
          analysisPrompt = `Analyze the competitive landscape for ${businessContext}. Identify key differentiators and opportunities.`;
          break;
        case "sentiment-analysis":
          analysisPrompt = `Analyze customer sentiment and feedback for ${businessContext}. Provide insights on customer satisfaction and improvement areas.`;
          break;
        default:
          analysisPrompt = `Provide business insights for ${businessContext} based on current market conditions.`;
      }

      // Generate insights using Grok AI
      if (!openai) {
        throw new Error("XAI_API_KEY not configured");
      }
      
      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a business analyst specializing in Hawaiian food and beverage businesses. Provide data-driven insights that are specific, actionable, and culturally aware."
          },
          {
            role: "user",
            content: analysisPrompt
          }
        ],
        max_tokens: 800,
      });

      const analysisContent = response.choices[0].message.content;

      // Save insight to database
      const insightData = insertAiInsightSchema.parse({
        businessId,
        title: `${type.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase())} Report`,
        content: analysisContent,
        type,
        category: "market-analysis",
        priority: "medium",
        actionable: true,
        metadata: { generated_at: new Date().toISOString() }
      });

      const savedInsight = await storage.createAiInsight(insightData);
      res.status(201).json(savedInsight);

    } catch (error: any) {
      console.error("Insight generation error:", error);
      res.status(500).json({ message: "Error generating insights: " + error.message });
    }
  });

  // Initialize businesses in database if they don't exist
  app.post("/api/init-businesses", async (req, res) => {
    try {
      const existingBusinesses = await storage.getBusinesses();
      
      if (existingBusinesses.length === 0) {
        // Create both businesses
        await storage.createBusiness({
          id: 'allii-fish-market',
          name: 'Allii Fish Market',
          type: 'restaurant',
          description: 'Authentic Hawaiian poke shop serving fresh, sustainable fish with traditional Hawaiian preparations.'
        });

        await storage.createBusiness({
          id: 'allii-coconut-water',
          name: 'Allii Coconut Water',
          type: 'beverage',
          description: 'Premium organic coconut water sourced from Hawaiian coconuts, emphasizing health and purity.'
        });

        res.json({ message: "Businesses initialized successfully" });
      } else {
        res.json({ message: "Businesses already exist" });
      }
    } catch (error: any) {
      res.status(500).json({ message: "Error initializing businesses: " + error.message });
    }
  });

  const httpServer = createServer(app);
  return httpServer;
}