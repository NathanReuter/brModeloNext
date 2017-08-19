/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ufsc.sisinf.brmodelo2all.control;

import java.util.List;

import com.mxgraph.model.mxCell;

import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.InheritanceObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;

/**
 *
 * @author Caca
 */
public class Messages {

	public static void fillAssociativeRelationsInteraction(mxCell relationCell, String[] messages,
			String[] alternatives) {
		messages[0] = "O que fazer a respeito do relacionamento associativo?"; // TODO:
																				// colocar
																				// nome
																				// do
																				// relacionamento
																				// assoc
																				// +
																				// relation.getName();

		alternatives[0] = "1) Criar uma única tabela para o relacionamento ";
		alternatives[1] = "2) Criar uma tabela para cada entidade";
	}

	public static void fillAttributeInteraction(AttributeObject attribute, String[] messages, String[] alternatives,
			int situation) {
		ModelingObject object = (ModelingObject) ((mxCell) (attribute.getParentObject())).getValue();

		switch (situation) {
		case 1: // atributo multivalorado maxCard = N / multivalorado maxCard =
				// valor fixo e quer criar a tabela.
			messages[0] = "Com relação a nova tabela criada para o atributo multivalorado ";

			if (attribute.isMultiValued()) {
			}
			messages[0] += "\"" + attribute.getName() + "\"";
			messages[1] = " encontrado na entidade \"";
			messages[1] += object.getName() + "\", como você deseja que seja sua chave primária?";

			alternatives[0] = "1) Apenas o próprio atributo : " + attribute.getName()
					+ " fará parte da chave primária.";
			alternatives[1] = "2) O próprio atributo : " + attribute.getName()
					+ " e também a chave estrangeira que referencia a tabela origem do atributo(" + object.getName()
					+ ") .";
			// alternatives[2] = "3) Deste ponto em diante aceitar todas as
			// sugestï¿½es";

			break;

		case 2: // atributo multivalorado maxCard = valor fixo
			messages[0] = "O que fazer a respeito do atributo multivalorado";
			messages[0] += "\"" + attribute.getName() + "\"";
			messages[1] = "encontrado na entidade \"";
			messages[1] += object.getName() + "\"?";

			alternatives[0] = "1) Criar uma tabela para acomodar o atributo.";
			alternatives[1] = "2) Incluir os atributos como campos na tabela ." + object.getName();
			// alternatives[2] = "3) Deste ponto em diante aceitar todas as
			// sugestï¿½es";
			break;
		}
	}

	public static void fillInheritanceInteraction(InheritanceObject inheritanceObject, String[] messages,
			String[] alternatives) {
		messages[0] = "O que fazer a respeito da especialização ";
		messages[0] += inheritanceObject.isPartial() ? "parcial e " : "total e ";
		messages[0] += inheritanceObject.isExclusive() ? "exclusiva " : "opcional ";
		messages[0] += " \"" + inheritanceObject.getName() + "\"";
		messages[1] = "encontrada partindo da entidade \"";
		messages[1] += ((ModelingObject) (((mxCell) (inheritanceObject.getParentObject())).getValue())).getName()
				+ "\"?";

		alternatives[0] = "1) Criar uma tabela para cada entidade";
		alternatives[1] = "2) Criar uma única tabela para toda a hierarquia";
		if (!inheritanceObject.isPartial()) {
			alternatives[2] = "3) Criar tabela(s) apenas para a(s) entidade(s) especializada(s)";
			alternatives[3] = "4) Deste ponto em diante aceitar todas as sugestões";

		} else {
			alternatives[2] = "3) Deste ponto em diante aceitar todas as sugestões";
		}
	}

	public static void fillRelationsInteraction(mxCell relationObject, String[] messages, String[] alternatives) {
		RelationObject relation = (RelationObject) relationObject.getValue();

		messages[0] = "O que fazer a respeito da relação ";
		messages[0] += "\"" + relation.getName() + "\"?";

		alternatives[0] = "1) Criar uma tabela para a relação";
		alternatives[1] = "2) Adicionar atributos à tabela de maior cardinalidade";
	}

	public static void fillRelationsInteractionOneOptional(mxCell relationObject, String[] messages,
			String[] alternatives) {
		RelationObject relation = (RelationObject) relationObject.getValue();

		messages[0] = "O que fazer a respeito da relação ";
		messages[0] += "\"" + relation.getName() + "\"?";

		alternatives[0] = "1) Excluir a tabela que possui cardinaliade opcional, agregando os seus atributos a tabela de maior cardinalidade";
		alternatives[1] = "2) Adicionar referÃªncia da tabela de maior cardinalidade a de menor cardinalidade";
	}

	public static void fillRelationsInteractionOptionalAll(mxCell relationObject, String[] messages,
			String[] alternatives) {
		RelationObject relation = (RelationObject) relationObject.getValue();

		messages[0] = "O que fazer a respeito da relação ";
		messages[0] += "\"" + relation.getName() + "\"?";

		alternatives[0] = "1) Cria uma tabela para a relação " + relation.getName();
		alternatives[1] = "2) Adicionar referência entre as tabelas";
	}

	public static void fillRelationsInteractionOptionalAttributtes(mxCell relationObject, EntityObject entityA,
			EntityObject entityB, String[] messages, String[] alternatives) {
		RelationObject relation = (RelationObject) relationObject.getValue();

		messages[0] = "Em qual tabela deseja adicionar os atributos da relação ";
		messages[0] += "\"" + relation.getName() + "\"?";

		alternatives[0] = "1) Adicionar atributos à  tabela " + entityA.getName();
		alternatives[1] = "2) Adicionar atributos à  tabela " + entityB.getName();
	}

	public static void fillSelfRelatedInteraction(EntityObject entity, RelationObject relation, String[] messages,
			String[] alternatives) {
		messages[0] = "O que fazer a respeito do auto-relacionamento ";
		messages[1] = "encontrado entre a entidade \"" + entity.getName() + "\" e o relacionamento \"";
		messages[1] += relation.getName() + "\"?";

		alternatives[0] = "1) Criar relacionamento recursivo em \"" + entity.getName() + "\"";
		alternatives[1] = "2) Criar uma tabela para o auto-relacionamento";
	}

	public static void fillTernOneAll(List<EntityObject> entidades, RelationObject relation, String[] messages,
			String[] alternatives) {
		messages[0] = "Qual o par de atributos será a chave primaria da tabela " + relation.getName();
		messages[1] = " criada pelo relacionamento ternário? ";

		alternatives[0] = "1) As chaves primárias das tabelas " + entidades.get(0).getName() + " e "
				+ entidades.get(1).getName();
		alternatives[1] = "2) As chaves primárias das tabelas " + entidades.get(0).getName() + " e "
				+ entidades.get(2).getName();
		alternatives[2] = "3) As chaves primárias das tabelas " + entidades.get(1).getName() + " e "
				+ entidades.get(2).getName();
	}

	public static void fillTernDOneN(List<EntityObject> entidades, RelationObject relation, String[] messages,
			String[] alternatives) {
		messages[0] = "Qual atributo fará parte da chave primaria da tabela " + relation.getName();
		messages[1] = " criada pelo relacionamento ternário? ";

		alternatives[0] = "1) A chave primária da tabela " + entidades.get(0).getName();
		alternatives[1] = "2) A chave primária da tabela " + entidades.get(1).getName();
	}

}
