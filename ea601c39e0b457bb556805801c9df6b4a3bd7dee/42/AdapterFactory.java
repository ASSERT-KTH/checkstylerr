/*******************************************************************************
 * Copyright (c) 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.jubula.graphiti.examples.chess.identifier;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.graphiti.examples.mm.chess.Board;
import org.eclipse.graphiti.examples.mm.chess.Colors;
import org.eclipse.graphiti.examples.mm.chess.Piece;
import org.eclipse.graphiti.examples.mm.chess.Square;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;
import org.eclipse.jubula.rc.rcp.gef.identifier.StaticEditPartIdentifier;

/**
 * Adapts EditParts (from the Graphiti Chess example) to IEditPartIdentifiers.
 */
@SuppressWarnings("restriction")
public class AdapterFactory implements IAdapterFactory {

    /** list of classes that can be adapted */
	@SuppressWarnings("rawtypes")
	private static final Class[] ADAPTER_LIST = 
			new Class[]{ContainerShapeEditPart.class};
	
	/** 
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ContainerShapeEditPart) {
			ContainerShapeEditPart editPart = (ContainerShapeEditPart)adaptableObject;
			Object businessObject =
					editPart.getFeatureProvider().getBusinessObjectForPictogramElement(editPart.getPictogramElement());
			if (businessObject instanceof Square) {
				Square square = (Square)businessObject;
				return new StaticEditPartIdentifier(square.getFile().toString().toLowerCase() + square.getRank().getValue());
			} else if (businessObject instanceof Piece) {
				Piece piece = (Piece)businessObject;
				String color = piece.getOwner() == Colors.LIGHT ? "w" : "b";
				return new StaticEditPartIdentifier(color + " " + piece.getType().toString());
			} else if (businessObject instanceof Board) {
				return new StaticEditPartIdentifier("board");
			}
		}
		
		return null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
