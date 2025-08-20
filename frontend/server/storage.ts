import { neon } from '@neondatabase/serverless';
import { drizzle } from 'drizzle-orm/neon-http';
import { eq, and, desc, asc } from 'drizzle-orm';
import * as schema from '@shared/schema';

if (!process.env.DATABASE_URL) {
  throw new Error('DATABASE_URL environment variable is required');
}

const sql = neon(process.env.DATABASE_URL);
export const db = drizzle(sql, { schema });

export interface IStorage {
  // Business operations
  getBusinesses(): Promise<schema.Business[]>;
  getBusiness(id: string): Promise<schema.Business | null>;
  createBusiness(business: schema.InsertBusiness): Promise<schema.Business>;

  // Customer operations
  getCustomers(businessId: string): Promise<schema.Customer[]>;
  getCustomer(businessId: string, id: number): Promise<schema.Customer | null>;
  createCustomer(customer: schema.InsertCustomer): Promise<schema.Customer>;
  updateCustomer(businessId: string, id: number, updates: Partial<schema.Customer>): Promise<schema.Customer>;
  deleteCustomer(businessId: string, id: number): Promise<void>;

  // Menu operations
  getMenuItems(businessId: string): Promise<schema.MenuItem[]>;
  getMenuItem(businessId: string, id: number): Promise<schema.MenuItem | null>;
  createMenuItem(menuItem: schema.InsertMenuItem): Promise<schema.MenuItem>;
  updateMenuItem(businessId: string, id: number, updates: Partial<schema.MenuItem>): Promise<schema.MenuItem>;
  deleteMenuItem(businessId: string, id: number): Promise<void>;

  // Inventory operations
  getInventoryItems(businessId: string): Promise<schema.InventoryItem[]>;
  getInventoryItem(businessId: string, id: number): Promise<schema.InventoryItem | null>;
  createInventoryItem(item: schema.InsertInventoryItem): Promise<schema.InventoryItem>;
  updateInventoryItem(businessId: string, id: number, updates: Partial<schema.InventoryItem>): Promise<schema.InventoryItem>;
  deleteInventoryItem(businessId: string, id: number): Promise<void>;

  // Order operations
  getOrders(businessId: string): Promise<schema.Order[]>;
  getOrder(businessId: string, id: number): Promise<schema.Order | null>;
  createOrder(order: schema.InsertOrder): Promise<schema.Order>;
  updateOrder(businessId: string, id: number, updates: Partial<schema.Order>): Promise<schema.Order>;
  deleteOrder(businessId: string, id: number): Promise<void>;

  // Content operations
  getGeneratedContent(businessId: string): Promise<schema.GeneratedContent[]>;
  getGeneratedContentItem(businessId: string, id: number): Promise<schema.GeneratedContent | null>;
  createGeneratedContent(content: schema.InsertGeneratedContent): Promise<schema.GeneratedContent>;
  updateGeneratedContent(businessId: string, id: number, updates: Partial<schema.GeneratedContent>): Promise<schema.GeneratedContent>;
  deleteGeneratedContent(businessId: string, id: number): Promise<void>;

  // AI Insights operations
  getAiInsights(businessId: string): Promise<schema.AiInsight[]>;
  getAiInsight(businessId: string, id: number): Promise<schema.AiInsight | null>;
  createAiInsight(insight: schema.InsertAiInsight): Promise<schema.AiInsight>;
  updateAiInsight(businessId: string, id: number, updates: Partial<schema.AiInsight>): Promise<schema.AiInsight>;
  deleteAiInsight(businessId: string, id: number): Promise<void>;
}

export class DatabaseStorage implements IStorage {
  // Business operations
  async getBusinesses(): Promise<schema.Business[]> {
    return await db.select().from(schema.businesses);
  }

  async getBusiness(id: string): Promise<schema.Business | null> {
    const results = await db.select().from(schema.businesses).where(eq(schema.businesses.id, id));
    return results[0] || null;
  }

  async createBusiness(business: schema.InsertBusiness): Promise<schema.Business> {
    const results = await db.insert(schema.businesses).values(business).returning();
    return results[0];
  }

  // Customer operations
  async getCustomers(businessId: string): Promise<schema.Customer[]> {
    return await db.select().from(schema.customers)
      .where(eq(schema.customers.businessId, businessId))
      .orderBy(desc(schema.customers.customerSince));
  }

  async getCustomer(businessId: string, id: number): Promise<schema.Customer | null> {
    const results = await db.select().from(schema.customers)
      .where(and(eq(schema.customers.businessId, businessId), eq(schema.customers.id, id)));
    return results[0] || null;
  }

  async createCustomer(customer: schema.InsertCustomer): Promise<schema.Customer> {
    const results = await db.insert(schema.customers).values(customer).returning();
    return results[0];
  }

  async updateCustomer(businessId: string, id: number, updates: Partial<schema.Customer>): Promise<schema.Customer> {
    const results = await db.update(schema.customers)
      .set(updates)
      .where(and(eq(schema.customers.businessId, businessId), eq(schema.customers.id, id)))
      .returning();
    return results[0];
  }

  async deleteCustomer(businessId: string, id: number): Promise<void> {
    await db.delete(schema.customers)
      .where(and(eq(schema.customers.businessId, businessId), eq(schema.customers.id, id)));
  }

  // Menu operations
  async getMenuItems(businessId: string): Promise<schema.MenuItem[]> {
    return await db.select().from(schema.menuItems)
      .where(eq(schema.menuItems.businessId, businessId))
      .orderBy(asc(schema.menuItems.category), asc(schema.menuItems.name));
  }

  async getMenuItem(businessId: string, id: number): Promise<schema.MenuItem | null> {
    const results = await db.select().from(schema.menuItems)
      .where(and(eq(schema.menuItems.businessId, businessId), eq(schema.menuItems.id, id)));
    return results[0] || null;
  }

  async createMenuItem(menuItem: schema.InsertMenuItem): Promise<schema.MenuItem> {
    const results = await db.insert(schema.menuItems).values(menuItem).returning();
    return results[0];
  }

  async updateMenuItem(businessId: string, id: number, updates: Partial<schema.MenuItem>): Promise<schema.MenuItem> {
    const results = await db.update(schema.menuItems)
      .set(updates)
      .where(and(eq(schema.menuItems.businessId, businessId), eq(schema.menuItems.id, id)))
      .returning();
    return results[0];
  }

  async deleteMenuItem(businessId: string, id: number): Promise<void> {
    await db.delete(schema.menuItems)
      .where(and(eq(schema.menuItems.businessId, businessId), eq(schema.menuItems.id, id)));
  }

  // Inventory operations
  async getInventoryItems(businessId: string): Promise<schema.InventoryItem[]> {
    return await db.select().from(schema.inventory)
      .where(eq(schema.inventory.businessId, businessId))
      .orderBy(asc(schema.inventory.category), asc(schema.inventory.name));
  }

  async getInventoryItem(businessId: string, id: number): Promise<schema.InventoryItem | null> {
    const results = await db.select().from(schema.inventory)
      .where(and(eq(schema.inventory.businessId, businessId), eq(schema.inventory.id, id)));
    return results[0] || null;
  }

  async createInventoryItem(item: schema.InsertInventoryItem): Promise<schema.InventoryItem> {
    const results = await db.insert(schema.inventory).values(item).returning();
    return results[0];
  }

  async updateInventoryItem(businessId: string, id: number, updates: Partial<schema.InventoryItem>): Promise<schema.InventoryItem> {
    const results = await db.update(schema.inventory)
      .set(updates)
      .where(and(eq(schema.inventory.businessId, businessId), eq(schema.inventory.id, id)))
      .returning();
    return results[0];
  }

  async deleteInventoryItem(businessId: string, id: number): Promise<void> {
    await db.delete(schema.inventory)
      .where(and(eq(schema.inventory.businessId, businessId), eq(schema.inventory.id, id)));
  }

  // Order operations
  async getOrders(businessId: string): Promise<schema.Order[]> {
    return await db.select().from(schema.orders)
      .where(eq(schema.orders.businessId, businessId))
      .orderBy(desc(schema.orders.orderDate));
  }

  async getOrder(businessId: string, id: number): Promise<schema.Order | null> {
    const results = await db.select().from(schema.orders)
      .where(and(eq(schema.orders.businessId, businessId), eq(schema.orders.id, id)));
    return results[0] || null;
  }

  async createOrder(order: schema.InsertOrder): Promise<schema.Order> {
    const results = await db.insert(schema.orders).values(order).returning();
    return results[0];
  }

  async updateOrder(businessId: string, id: number, updates: Partial<schema.Order>): Promise<schema.Order> {
    const results = await db.update(schema.orders)
      .set(updates)
      .where(and(eq(schema.orders.businessId, businessId), eq(schema.orders.id, id)))
      .returning();
    return results[0];
  }

  async deleteOrder(businessId: string, id: number): Promise<void> {
    await db.delete(schema.orders)
      .where(and(eq(schema.orders.businessId, businessId), eq(schema.orders.id, id)));
  }

  // Content operations
  async getGeneratedContent(businessId: string): Promise<schema.GeneratedContent[]> {
    return await db.select().from(schema.generatedContent)
      .where(eq(schema.generatedContent.businessId, businessId))
      .orderBy(desc(schema.generatedContent.aiGeneratedAt));
  }

  async getGeneratedContentItem(businessId: string, id: number): Promise<schema.GeneratedContent | null> {
    const results = await db.select().from(schema.generatedContent)
      .where(and(eq(schema.generatedContent.businessId, businessId), eq(schema.generatedContent.id, id)));
    return results[0] || null;
  }

  async createGeneratedContent(content: schema.InsertGeneratedContent): Promise<schema.GeneratedContent> {
    const results = await db.insert(schema.generatedContent).values(content).returning();
    return results[0];
  }

  async updateGeneratedContent(businessId: string, id: number, updates: Partial<schema.GeneratedContent>): Promise<schema.GeneratedContent> {
    const results = await db.update(schema.generatedContent)
      .set(updates)
      .where(and(eq(schema.generatedContent.businessId, businessId), eq(schema.generatedContent.id, id)))
      .returning();
    return results[0];
  }

  async deleteGeneratedContent(businessId: string, id: number): Promise<void> {
    await db.delete(schema.generatedContent)
      .where(and(eq(schema.generatedContent.businessId, businessId), eq(schema.generatedContent.id, id)));
  }

  // AI Insights operations
  async getAiInsights(businessId: string): Promise<schema.AiInsight[]> {
    return await db.select().from(schema.aiInsights)
      .where(eq(schema.aiInsights.businessId, businessId))
      .orderBy(desc(schema.aiInsights.createdAt));
  }

  async getAiInsight(businessId: string, id: number): Promise<schema.AiInsight | null> {
    const results = await db.select().from(schema.aiInsights)
      .where(and(eq(schema.aiInsights.businessId, businessId), eq(schema.aiInsights.id, id)));
    return results[0] || null;
  }

  async createAiInsight(insight: schema.InsertAiInsight): Promise<schema.AiInsight> {
    const results = await db.insert(schema.aiInsights).values(insight).returning();
    return results[0];
  }

  async updateAiInsight(businessId: string, id: number, updates: Partial<schema.AiInsight>): Promise<schema.AiInsight> {
    const results = await db.update(schema.aiInsights)
      .set(updates)
      .where(and(eq(schema.aiInsights.businessId, businessId), eq(schema.aiInsights.id, id)))
      .returning();
    return results[0];
  }

  async deleteAiInsight(businessId: string, id: number): Promise<void> {
    await db.delete(schema.aiInsights)
      .where(and(eq(schema.aiInsights.businessId, businessId), eq(schema.aiInsights.id, id)));
  }
}

export const storage = new DatabaseStorage();