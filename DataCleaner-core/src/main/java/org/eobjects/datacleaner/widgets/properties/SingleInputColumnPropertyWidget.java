/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JRadioButton;

import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.jdesktop.swingx.JXRadioGroup;

public class SingleInputColumnPropertyWidget extends AbstractPropertyWidget<InputColumn<?>> implements
		SourceColumnChangeListener, TransformerChangeListener {

	private static final long serialVersionUID = 1L;
	private final JXRadioGroup<JRadioButton> _radioGroup = new JXRadioGroup<JRadioButton>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final DataTypeFamily _dataTypeFamily;
	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private volatile JRadioButton[] _radioButtons;
	private volatile List<InputColumn<?>> _inputColumns;
	private final InputColumnPropertyWidgetAccessoryHandler _accessoryHandler;

	public SingleInputColumnPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_radioGroup.setLayoutAxis(BoxLayout.Y_AXIS);
		_radioGroup.setOpaque(false);
		_analysisJobBuilder = analysisJobBuilder;
		_analysisJobBuilder.getSourceColumnListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_beanJobBuilder = beanJobBuilder;
		_propertyDescriptor = propertyDescriptor;
		_dataTypeFamily = propertyDescriptor.getInputColumnDataTypeFamily();

		if (_dataTypeFamily == DataTypeFamily.STRING || _dataTypeFamily == DataTypeFamily.UNDEFINED) {
			_accessoryHandler = new InputColumnPropertyWidgetAccessoryHandler(_propertyDescriptor, _beanJobBuilder, this,
					true);
		} else {
			_accessoryHandler = null;
		}

		updateComponents();
		add(_radioGroup);

	}

	private void updateComponents() {
		_inputColumns = _analysisJobBuilder.getAvailableInputColumns(_dataTypeFamily);

		if (_beanJobBuilder instanceof TransformerJobBuilder) {
			// remove all the columns that are generated by the transformer
			// itself!
			TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) _beanJobBuilder;
			List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
			_inputColumns.removeAll(outputColumns);
		}

		InputColumn<?> currentValue = (InputColumn<?>) _beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
		if (currentValue != null) {
			if (!_inputColumns.contains(currentValue)) {
				_inputColumns.add(currentValue);
			}
		}

		if (_propertyDescriptor.isRequired()) {
			_radioButtons = new JRadioButton[_inputColumns.size()];
		} else {
			_radioButtons = new JRadioButton[_inputColumns.size() + 1];
		}
		if (_inputColumns.isEmpty()) {
			_radioButtons = new JRadioButton[1];
			JRadioButton radioButton = new JRadioButton("- no columns available -");
			radioButton.setOpaque(false);
			radioButton.setEnabled(false);
			_radioButtons[0] = radioButton;
		} else {
			for (int i = 0; i < _inputColumns.size(); i++) {
				InputColumn<?> inputColumn = _inputColumns.get(i);
				JRadioButton radioButton = new JRadioButton(inputColumn.getName());
				radioButton.setOpaque(false);
				if (currentValue == inputColumn) {
					radioButton.setSelected(true);
				}
				_radioButtons[i] = radioButton;
			}

			if (!_propertyDescriptor.isRequired()) {
				JRadioButton radioButton = new JRadioButton("(none)");
				radioButton.setOpaque(false);
				if (currentValue == null) {
					radioButton.setSelected(true);
				}
				_radioButtons[_radioButtons.length - 1] = radioButton;
			}
		}

		for (int i = 0; i < _radioButtons.length; i++) {
			JRadioButton rb = _radioButtons[i];

			rb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					fireValueChanged();

				}
			});

			if (_accessoryHandler != null) {
				_accessoryHandler.registerListComponent(rb, null);
			}
		}

		_radioGroup.setValues(_radioButtons);
		fireValueChanged();
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		if (_dataTypeFamily == DataTypeFamily.UNDEFINED || _dataTypeFamily == sourceColumn.getDataTypeFamily()) {
			updateComponents();
			updateUI();
		}
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		if (_dataTypeFamily == DataTypeFamily.UNDEFINED || _dataTypeFamily == sourceColumn.getDataTypeFamily()) {
			InputColumn<?> currentValue = (InputColumn<?>) _beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
			if (currentValue != null) {
				if (currentValue.equals(sourceColumn)) {
					_beanJobBuilder.setConfiguredProperty(_propertyDescriptor, null);
				}
			}
			updateComponents();
			updateUI();
		}
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		updateComponents();
		updateUI();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_analysisJobBuilder.getSourceColumnListeners().remove(this);
		_analysisJobBuilder.getTransformerChangeListeners().remove(this);
	}

	@Override
	public InputColumn<?> getValue() {
		for (int i = 0; i < _inputColumns.size(); i++) {
			JRadioButton radio = _radioButtons[i];
			if (radio.isSelected()) {
				return _inputColumns.get(i);
			}
		}
		return null;
	}

	@Override
	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
	}

	@Override
	protected void setValue(InputColumn<?> value) {
		updateComponents();
		updateUI();
	}
}