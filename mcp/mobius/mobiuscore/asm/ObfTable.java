package mcp.mobius.mobiuscore.asm;

import java.io.IOException;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

import cpw.mods.fml.common.FMLCommonHandler;

public enum ObfTable {
	WORLD_UPDATEENTITIES      ("abw", "h", "()V",      "net/minecraft/world/World",           "updateEntities", "()V"),
	WORLD_UPDATEENTITY        ("abw", "g", "(Lnn;)V",  "net/minecraft/world/World",           "updateEntity",   "(Lnet/minecraft/entity/Entity;)V"),
	TILEENTITY_UPDATEENTITY   ("asp", "h", "()V",      "net/minecraft/tileentity/TileEntity", "updateEntity",   "()V"),
	NETWORKLISTEN_NETWORKTICK ("kd",  "b", "()V",      "net/minecraft/network/NetworkListenThread",          "networkTick",   "()V"),
	RENDERMANAGER_RENDERENTITY("bgl", "a", "(Lnn;F)V", "net/minecraft/client/renderer/entity/RenderManager", "renderEntity",  "(Lnet/minecraft/entity/Entity;F)V"),
	TCPCONN_READPACKET		  ("co",  "i", "()Z",	   "net/minecraft/network/TcpConnection",                "readPacket",    "()Z"),
	TCPCONN_SENDPACKET        ("co",  "a", "(Z)Ley;",  "net/minecraft/network/TcpConnection",                "func_74460_a",  "(Z)Lnet/minecraft/network/packet/Packet;"),
	TCPCONN_NETWORKSOCKET     ("co",  "j", "Ljava/net/Socket;", "net/minecraft/network/TcpConnection",       "networkSocket", "Ljava/net/Socket;"),
	
	MEMCONN_ADDSENDQUEUE      ("cn",  "a", "(Ley;)V", "net/minecraft/network/MemoryConnection", "addToSendQueue", "(Lnet/minecraft/network/packet/Packet;)V"),
	MEMCONN_PROCESSREAD       ("cn",  "b", "()V", "net/minecraft/network/MemoryConnection", "processReadPackets", "()V"),
	MEMCONN_PAIREDCONN        ("cn",  "d", "Lcn;",    "net/minecraft/network/MemoryConnection", "pairedConnection",     "Lnet/minecraft/network/MemoryConnection;"),
    MEMCONN_PROCESSORCACHE    ("cn",  "b", "(Ley;)V", "net/minecraft/network/MemoryConnection", "processOrCachePacket", "(Lnet/minecraft/network/packet/Packet;)V"),	
	
    MEMCONN_MYNETHANDLER      ("cn", "e", "Lez;", "net/minecraft/network/MemoryConnection", "myNetHandler", "Lnet/minecraft/network/packet/NetHandler;"),
    PACKET_PROCESSPACKET      ("ey", "a", "(Lez;)V", "net/minecraft/network/packet/Packet", "processPacket", "(Lnet/minecraft/network/packet/NetHandler;)V"),    
    
	PACKET_WRITEPACKET        ("ey",  "a", "(Llp;Ljava/io/DataInput;ZLjava/net/Socket;)Ley;", "net/minecraft/network/packet/Packet", "writePacket", "(Lnet/minecraft/network/packet/Packet;Ljava/io/DataOutput;)V"),
	PACKET_READPACKET         ("ey",  "a", "(Llp;Ljava/io/DataInput;ZLjava/net/Socket;)Ley;", "net/minecraft/network/packet/Packet", "readPacket", "(Lnet/minecraft/logging/ILogAgent;Ljava/io/DataInput;ZLjava/net/Socket;)Lnet/minecraft/network/packet/Packet;"),
	TERENDER_RENDERAT         ("bjd", "a", "(Lasp;DDDF)V", "net/minecraft/client/renderer/tileentity/TileEntityRenderer", "renderTileEntityAt", "(Lnet/minecraft/tileentity/TileEntity;DDDF)V"),
	WORLDSERVER_TICK          ("js",  "b", "()V",   "net/minecraft/world/WorldServer",   "tick",        "()V"),
	WORLD_PROVIDER            ("js",  "t", "Laei;", "net/minecraft/world/WorldServer",   "provider",    "Lnet/minecraft/world/WorldProvider;"),
	WORLDPROVIDER_DIMID       ("aei", "i", "I",     "net/minecraft/world/WorldProvider", "dimensionId", "I");
	
	
	private static Boolean isObfuscated = null;
	private static Boolean isCauldron   = null;	
	private String clazzNameN;
	private String methodNameN;
	private String descriptorN;
	private String clazzNameS;
	private String methodNameS;
	private String descriptorS;	
	
	private ObfTable(String clazzn, String namen, String descn, String clazzs, String names, String descs){
		this.clazzNameN = clazzn;
		this.clazzNameS = clazzs;
		this.methodNameN = namen;
		this.methodNameS = names;
		this.descriptorN = descn;
		this.descriptorS = descs;
	}
	
	public String getClazz(){
		if (deobfuscatedEnvironment())
			return this.clazzNameS;
		else
			return this.clazzNameN;
	}

	public String getName(){
		if (deobfuscatedEnvironment())
			return this.methodNameS;
		else
			return this.methodNameN;
	}	

	public String getDescriptor(){
		if (deobfuscatedEnvironment())
			return this.descriptorS;
		else
			return this.descriptorN;
	}		
	
	public String getFullDescriptor(){
		if (deobfuscatedEnvironment())
			return this.methodNameS + " " + this.descriptorS;
		else
			return this.methodNameN + " " + this.descriptorN;
	}
	
	public static Boolean deobfuscatedEnvironment(){
		if (ObfTable.isObfuscated != null)
			return ObfTable.isObfuscated;
		
		try{
		// Are we in a 'decompiled' environment?
			byte[] bs = ((LaunchClassLoader)CoreContainer.class.getClassLoader()).getClassBytes("net.minecraft.world.World");
			if (bs != null){
				System.out.printf("[MobiusCore] Current code is UNOBFUSCATED\n");
				ObfTable.isObfuscated = true;
			} else {
				System.out.printf("[MobiusCore] Current code is OBFUSCATED\n");
				ObfTable.isObfuscated = false;
			}

		}catch (IOException e1){}
		
		return ObfTable.isObfuscated;
	}	
	
	public static Boolean isCauldron(){
		if (ObfTable.isCauldron != null)
			return ObfTable.isCauldron;		
		
		ObfTable.isCauldron = FMLCommonHandler.instance().getModName().contains("cauldron") || FMLCommonHandler.instance().getModName().contains("mcpc"); 
		if (ObfTable.isCauldron)
			System.out.printf("[MobiusCore] Switching injection mode to CAULDRON\n");
		else
			System.out.printf("[MobiusCore] Switching injection mode to FORGE\n");
		
		return ObfTable.isCauldron; 
	}	
	
	/*
	public FieldInsnNode getFieldNode(){
		return new FieldInsnNode(Opcodes.GETFIELD, this.getClazz(), this.getName(), this.getDescriptor());
	}
	*/
}