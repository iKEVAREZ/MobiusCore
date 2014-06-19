package mcp.mobius.mobiuscore.asm.transformers.common;

import java.util.ArrayList;
import java.util.ListIterator;

import mcp.mobius.mobiuscore.asm.ObfTable;
import mcp.mobius.mobiuscore.asm.Opcode;
import mcp.mobius.mobiuscore.asm.transformers.TransformerBase;
import mcp.mobius.mobiuscore.profiler.ProfilerSection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformerMemoryConnection extends TransformerBase {

	private static String MEMCON_READPACKET;
	private static String MEMCON_SENDPACKET;	

	private static AbstractInsnNode[] MEMCON_PATTERN_PROCESSPACKET;		
	private static AbstractInsnNode[] MEMCON_PATTERN_ADDSENDQUEUE;	
	private static AbstractInsnNode[] MEMCON_PAYLOAD_INPACKET;
	private static AbstractInsnNode[] MEMCON_PAYLOAD_OUTPACKET;	
	
	static{
		String profilerClass =  ProfilerSection.getClassName();
		String profilerType  =  ProfilerSection.getTypeName();
		
		MEMCON_READPACKET   = ObfTable.MEMCONN_PROCESSREAD.getFullDescriptor();
		MEMCON_SENDPACKET   = ObfTable.MEMCONN_ADDSENDQUEUE.getFullDescriptor();		
		
		MEMCON_PATTERN_ADDSENDQUEUE =	new AbstractInsnNode[]{
			    Opcode.ALOAD(-1),
			    Opcode.GETFIELD(ObfTable.MEMCONN_PAIREDCONN.getClazz(), ObfTable.MEMCONN_PAIREDCONN.getName(), ObfTable.MEMCONN_PAIREDCONN.getDescriptor()),
			    Opcode.ALOAD(-1),
			    Opcode.INVOKEVIRTUAL(ObfTable.MEMCONN_PROCESSORCACHE.getClazz(), ObfTable.MEMCONN_PROCESSORCACHE.getName(), ObfTable.MEMCONN_PROCESSORCACHE.getDescriptor()),
				};
		
		MEMCON_PATTERN_PROCESSPACKET =	new AbstractInsnNode[]{
				Opcode.ALOAD(-1),
				Opcode.ALOAD(-1),
				Opcode.GETFIELD(ObfTable.MEMCONN_MYNETHANDLER.getClazz(), ObfTable.MEMCONN_MYNETHANDLER.getName(), ObfTable.MEMCONN_MYNETHANDLER.getDescriptor()),
				Opcode.INVOKEVIRTUAL(ObfTable.PACKET_PROCESSPACKET.getClazz(), ObfTable.PACKET_PROCESSPACKET.getName(), ObfTable.PACKET_PROCESSPACKET.getDescriptor())
				};		
		
		MEMCON_PAYLOAD_INPACKET =	new AbstractInsnNode[]{ 
				 Opcode.GETSTATIC(profilerClass, ProfilerSection.PACKET_INBOUND.name() , profilerType),
				 Opcode.ALOAD(2), 
				 Opcode.INVOKEVIRTUAL(profilerClass, "start", "(Ljava/lang/Object;)V")};
		
		MEMCON_PAYLOAD_OUTPACKET = new AbstractInsnNode[]{
				 Opcode.GETSTATIC(profilerClass, ProfilerSection.PACKET_OUTBOUND.name() , profilerType),
				 Opcode.ALOAD(1), 
				 Opcode.INVOKEVIRTUAL(profilerClass, "start", "(Ljava/lang/Object;)V")};				
	}	
	
	@Override
	public byte[] transform(String name, String srgname, byte[] bytes) {
		this.dumpChecksum(bytes, name, srgname);
		
		ClassNode   classNode   = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);		
		
        classReader.accept(classNode, 0);
		
        MethodNode readPacketNode  = this.getMethod(classNode, MEMCON_READPACKET);
        this.applyPayloadBefore(readPacketNode, MEMCON_PATTERN_PROCESSPACKET, MEMCON_PAYLOAD_INPACKET);
        
        MethodNode sendPacketNode  = this.getMethod(classNode, MEMCON_SENDPACKET);
        this.applyPayloadBefore(sendPacketNode, MEMCON_PATTERN_ADDSENDQUEUE, MEMCON_PAYLOAD_OUTPACKET);         
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
	}

}